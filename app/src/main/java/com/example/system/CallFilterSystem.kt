package com.example.system

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.OsPreferences
import com.example.data.OsPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class CallScreenActionType {
    ALLOWED_CONTACT,
    ALLOWED_BY_USER,
    REJECTED_BY_USER,
    PENDING_UNKNOWN,
}

data class ScreenedCallRecord(
    val id: String,
    val rawNumber: String,
    val normalizedNumber: String,
    val contactName: String?,
    val status: CallScreenActionType,
    val timestampMs: Long,
)

data class PendingUnknownCallPrompt(
    val id: String,
    val rawNumber: String,
    val normalizedNumber: String,
    val timestampMs: Long,
)

object CallFilterEventBus {
    private val _pendingUnknown = MutableSharedFlow<PendingUnknownCallPrompt>(extraBufferCapacity = 8)
    val pendingUnknown: SharedFlow<PendingUnknownCallPrompt> = _pendingUnknown.asSharedFlow()

    fun emitUnknown(prompt: PendingUnknownCallPrompt) {
        _pendingUnknown.tryEmit(prompt)
    }
}

class CallFilterManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "miui_call_filter_channel"
        const val ACTION_ALLOW_CALL = "com.example.action.ALLOW_CALL"
        const val ACTION_REJECT_CALL = "com.example.action.REJECT_CALL"
        const val EXTRA_NUMBER = "extra_number"
        const val EXTRA_NOTIF_ID = "extra_notif_id"
    }

    fun normalizePhoneNumber(raw: String): String {
        val trimmed = raw.trim()
        val hasPlus = trimmed.startsWith("+")
        val digits = trimmed.filter { it.isDigit() }
        return if (hasPlus && digits.isNotEmpty()) "+$digits" else digits
    }

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isCallScreeningRoleHeld(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            }
        }
        return false
    }

    fun createCallScreeningRoleIntent(): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null &&
                roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            ) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            }
        }
        return null
    }

    /**
     * Checks device ContactsContract.PhoneLookup to determine if caller is a saved contact.
     */
    fun lookupContactName(rawNumber: String): String? {
        if (!hasContactsPermission() || rawNumber.isBlank()) return null
        return runCatching {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(rawNumber),
            )
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    fun evaluateNumber(
        rawNumber: String,
        prefs: OsPreferences,
    ): Pair<CallScreenActionType, String?> {
        val contactName = lookupContactName(rawNumber)
        if (!contactName.isNullOrBlank()) {
            return CallScreenActionType.ALLOWED_CONTACT to contactName
        }
        val normalized = normalizePhoneNumber(rawNumber)
        if (prefs.allowedCallNumbers.contains(normalized)) {
            return CallScreenActionType.ALLOWED_BY_USER to null
        }
        if (prefs.blockedCallNumbers.contains(normalized)) {
            return CallScreenActionType.REJECTED_BY_USER to null
        }
        return CallScreenActionType.PENDING_UNKNOWN to null
    }

    fun postUnknownCallerNotification(rawNumber: String, normalized: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Фильтрация неизвестных звонков",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Запрос разрешения или отклонения для звонков не из контактов"
            }
            nm.createNotificationChannel(channel)
        }

        val notifId = (normalized.hashCode() and 0x7FFFFFFF).coerceAtLeast(1001)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPi = PendingIntent.getActivity(
            context,
            notifId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val allowIntent = Intent(context, CallFilterActionReceiver::class.java).apply {
            action = ACTION_ALLOW_CALL
            putExtra(EXTRA_NUMBER, normalized)
            putExtra(EXTRA_NOTIF_ID, notifId)
        }
        val allowPi = PendingIntent.getBroadcast(
            context,
            notifId + 1,
            allowIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val rejectIntent = Intent(context, CallFilterActionReceiver::class.java).apply {
            action = ACTION_REJECT_CALL
            putExtra(EXTRA_NUMBER, normalized)
            putExtra(EXTRA_NOTIF_ID, notifId)
        }
        val rejectPi = PendingIntent.getBroadcast(
            context,
            notifId + 2,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val hasNotifPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

        if (!hasNotifPermission) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentTitle("Неизвестный звонок: $rawNumber")
            .setContentText("Номера нет в контактах. Разрешить или отклонить?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .addAction(android.R.drawable.ic_menu_call, "Разрешить", allowPi)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Отклонить", rejectPi)
            .build()

        nm.notify(notifId, notification)
    }

    fun parseHistory(json: String): List<ScreenedCallRecord> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                ScreenedCallRecord(
                    id = obj.getString("id"),
                    rawNumber = obj.getString("rawNumber"),
                    normalizedNumber = obj.getString("normalizedNumber"),
                    contactName = obj.optString("contactName").takeIf { it.isNotBlank() },
                    status = runCatching {
                        CallScreenActionType.valueOf(obj.getString("status"))
                    }.getOrDefault(CallScreenActionType.PENDING_UNKNOWN),
                    timestampMs = obj.optLong("timestampMs", 0L),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun serializeHistory(list: List<ScreenedCallRecord>): String {
        val arr = JSONArray()
        list.take(30).forEach { rec ->
            arr.put(
                JSONObject().apply {
                    put("id", rec.id)
                    put("rawNumber", rec.rawNumber)
                    put("normalizedNumber", rec.normalizedNumber)
                    put("contactName", rec.contactName ?: "")
                    put("status", rec.status.name)
                    put("timestampMs", rec.timestampMs)
                },
            )
        }
        return arr.toString()
    }
}

/**
 * Android 10+ system CallScreeningService.
 * - Contacts & user-allowed numbers pass immediately.
 * - Blocked numbers are rejected cleanly.
 * - Unknown numbers trigger an interactive notification ("Разрешить" / "Отклонить") and live UI prompt.
 */
class MiuiCallScreeningService : CallScreeningService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val isIncoming = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        } else {
            true
        }
        if (!isIncoming) {
            respondToCall(callDetails, CallResponse.Builder().setDisallowCall(false).build())
            return
        }

        val rawNumber = callDetails.handle?.schemeSpecificPart.orEmpty().ifBlank { "Скрытый номер" }
        val manager = CallFilterManager(applicationContext)
        val prefsRepo = OsPreferencesRepository(applicationContext)

        scope.launch {
            val prefs = prefsRepo.preferencesFlow.first()
            if (!prefs.callFilterEnabled) {
                respondToCall(callDetails, CallResponse.Builder().setDisallowCall(false).build())
                return@launch
            }

            val (decision, contactName) = manager.evaluateNumber(rawNumber, prefs)
            val normalized = manager.normalizePhoneNumber(rawNumber)

            when (decision) {
                CallScreenActionType.ALLOWED_CONTACT,
                CallScreenActionType.ALLOWED_BY_USER,
                -> {
                    respondToCall(
                        callDetails,
                        CallResponse.Builder()
                            .setDisallowCall(false)
                            .setRejectCall(false)
                            .setSilenceCall(false)
                            .build(),
                    )
                }

                CallScreenActionType.REJECTED_BY_USER -> {
                    respondToCall(
                        callDetails,
                        CallResponse.Builder()
                            .setDisallowCall(true)
                            .setRejectCall(true)
                            .setSkipCallLog(false)
                            .setSkipNotification(true)
                            .build(),
                    )
                }

                CallScreenActionType.PENDING_UNKNOWN -> {
                    // Silence ring until user decides "Разрешить" or "Отклонить" in the notification/banner
                    respondToCall(
                        callDetails,
                        CallResponse.Builder()
                            .setDisallowCall(false)
                            .setSilenceCall(true)
                            .build(),
                    )
                    manager.postUnknownCallerNotification(rawNumber, normalized)
                    CallFilterEventBus.emitUnknown(
                        PendingUnknownCallPrompt(
                            id = UUID.randomUUID().toString(),
                            rawNumber = rawNumber,
                            normalizedNumber = normalized,
                            timestampMs = System.currentTimeMillis(),
                        ),
                    )
                }
            }

            val existing = manager.parseHistory(prefs.callHistoryJson)
            val record = ScreenedCallRecord(
                id = UUID.randomUUID().toString(),
                rawNumber = rawNumber,
                normalizedNumber = normalized,
                contactName = contactName,
                status = decision,
                timestampMs = System.currentTimeMillis(),
            )
            prefsRepo.setCallHistoryJson(manager.serializeHistory(listOf(record) + existing))
        }
    }
}

class CallFilterActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val number = intent.getStringExtra(CallFilterManager.EXTRA_NUMBER) ?: return
        val notifId = intent.getIntExtra(CallFilterManager.EXTRA_NOTIF_ID, -1)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = OsPreferencesRepository(context.applicationContext)
                val manager = CallFilterManager(context.applicationContext)
                val isAllow = intent.action == CallFilterManager.ACTION_ALLOW_CALL

                if (isAllow) {
                    repo.allowCallNumber(number)
                } else {
                    repo.blockCallNumber(number)
                }

                val prefs = repo.preferencesFlow.first()
                val updatedHistory = manager.parseHistory(prefs.callHistoryJson).map { rec ->
                    if (rec.normalizedNumber == number && rec.status == CallScreenActionType.PENDING_UNKNOWN) {
                        rec.copy(
                            status = if (isAllow) CallScreenActionType.ALLOWED_BY_USER else CallScreenActionType.REJECTED_BY_USER,
                        )
                    } else {
                        rec
                    }
                }
                repo.setCallHistoryJson(manager.serializeHistory(updatedHistory))

                if (notifId != -1) {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    nm?.cancel(notifId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
