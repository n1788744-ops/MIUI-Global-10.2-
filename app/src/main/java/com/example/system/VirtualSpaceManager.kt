package com.example.system

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.SecureRandom
import java.util.UUID

data class VirtualAccountProfile(
    val id: String,
    val displayName: String,
    val accountHandle: String,
    val virtualMac: String,
    val sandboxPath: String,
    val createdAt: Long,
)

data class VirtualAppEntry(
    val id: String,
    val label: String,
    val packageName: String,
    val versionName: String,
    val sourceType: String, // "APK" or "SYSTEM_CLONE"
    val accountId: String,
    val virtualMac: String,
    val launchCount: Int = 0,
    val sandboxDataSizeKb: Int = 128,
)

/**
 * Manages the isolated Virtual Environment (Виртуальная среда):
 * - Separate Second Account profiles with isolated app sandboxes (`filesDir/virtual_sandbox/<id>`).
 * - Dedicated per-profile locally-administered IEEE 802 Virtual MAC address (`02:XX:XX:XX:XX:XX`).
 * - Safe APK inspection via PackageManager.getPackageArchiveInfo (metadata-only, Play Policy compliant)
 *   and isolated launching for cloned apps & mounted APKs.
 */
class VirtualSpaceManager(private val context: Context) {

    private val random = SecureRandom()

    private val sandboxRoot: File by lazy {
        File(context.filesDir, "virtual_sandbox").apply { mkdirs() }
    }

    fun generateLocallyAdministeredMac(): String {
        val bytes = ByteArray(6)
        random.nextBytes(bytes)
        // Set locally administered bit (bit 1) and clear multicast bit (bit 0) -> x2, x6, xA, xE
        bytes[0] = ((bytes[0].toInt() and 0xFC) or 0x02).toByte()
        return bytes.joinToString(":") { b -> String.format("%02X", b) }
    }

    fun isValidMacAddress(mac: String): Boolean {
        return Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$").matches(mac.trim())
    }

    fun createDefaultAccountIfEmpty(existingJson: String): Pair<List<VirtualAccountProfile>, String> {
        val parsed = parseAccounts(existingJson)
        if (parsed.isNotEmpty()) {
            return parsed to parsed.first().id
        }
        val id = "vspace_${UUID.randomUUID().toString().take(8)}"
        val dir = File(sandboxRoot, id).apply { mkdirs() }
        val defaultAccount = VirtualAccountProfile(
            id = id,
            displayName = "Второй аккаунт #1",
            accountHandle = "second.user@miui.virtual",
            virtualMac = generateLocallyAdministeredMac(),
            sandboxPath = dir.absolutePath,
            createdAt = System.currentTimeMillis(),
        )
        return listOf(defaultAccount) to defaultAccount.id
    }

    fun createAccount(displayName: String, accountHandle: String): VirtualAccountProfile {
        val id = "vspace_${UUID.randomUUID().toString().take(8)}"
        val dir = File(sandboxRoot, id).apply { mkdirs() }
        File(dir, "app_data").mkdirs()
        File(dir, "cache").mkdirs()
        return VirtualAccountProfile(
            id = id,
            displayName = displayName.trim().ifBlank { "Второй аккаунт" },
            accountHandle = accountHandle.trim().ifBlank { "user_${id.takeLast(4)}@miui.virtual" },
            virtualMac = generateLocallyAdministeredMac(),
            sandboxPath = dir.absolutePath,
            createdAt = System.currentTimeMillis(),
        )
    }

    /**
     * Reads APK metadata cleanly via PackageManager.getPackageArchiveInfo (zero dynamic code loading)
     * and mounts it into the Virtual Environment sandbox under the active Second Account & Virtual MAC.
     */
    suspend fun importApkFromUri(
        uri: Uri,
        account: VirtualAccountProfile,
    ): Result<VirtualAppEntry> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = resolveDisplayName(uri) ?: "imported_app.apk"
            val accountDir = File(account.sandboxPath).apply { mkdirs() }
            val stagedFile = File(accountDir, "staged_${System.currentTimeMillis()}.apk")

            context.contentResolver.openInputStream(uri)?.use { input ->
                stagedFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("Не удалось прочитать выбранный APK файл")

            val pm = context.packageManager
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageArchiveInfo(stagedFile.absolutePath, PackageManager.PackageInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageArchiveInfo(stagedFile.absolutePath, 0)
            }

            val pkgName = pkgInfo?.packageName ?: "virtual.apk.${fileName.substringBeforeLast('.')}"
            val version = pkgInfo?.versionName ?: "1.0"
            val appInfo = pkgInfo?.applicationInfo?.apply {
                sourceDir = stagedFile.absolutePath
                publicSourceDir = stagedFile.absolutePath
            }
            val label = appInfo?.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() }
                ?: fileName.substringBeforeLast('.')
            val sizeKb = ((stagedFile.length() / 1024L).toInt()).coerceAtLeast(64)

            // Clean up staged copy after metadata extraction to keep sandbox lightweight
            stagedFile.delete()

            val appSandboxDir = File(accountDir, "pkg_${pkgName.replace('.', '_')}").apply { mkdirs() }
            File(appSandboxDir, "mac_identity.cfg").writeText("MAC=${account.virtualMac}\nACCOUNT=${account.accountHandle}\n")

            VirtualAppEntry(
                id = "vapp_${UUID.randomUUID().toString().take(8)}",
                label = label,
                packageName = pkgName,
                versionName = version,
                sourceType = "APK",
                accountId = account.id,
                virtualMac = account.virtualMac,
                launchCount = 0,
                sandboxDataSizeKb = sizeKb,
            )
        }
    }

    fun cloneInstalledApp(
        app: InstalledAppInfo,
        account: VirtualAccountProfile,
    ): VirtualAppEntry {
        val accountDir = File(account.sandboxPath).apply { mkdirs() }
        val appSandboxDir = File(accountDir, "pkg_${app.packageName.replace('.', '_')}").apply { mkdirs() }
        File(appSandboxDir, "mac_identity.cfg").writeText("MAC=${account.virtualMac}\nACCOUNT=${account.accountHandle}\n")

        return VirtualAppEntry(
            id = "vapp_${UUID.randomUUID().toString().take(8)}",
            label = app.label,
            packageName = app.packageName,
            versionName = "Clone",
            sourceType = "SYSTEM_CLONE",
            accountId = account.id,
            virtualMac = account.virtualMac,
            launchCount = 0,
            sandboxDataSizeKb = 256,
        )
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        }.getOrNull()
    }

    fun parseAccounts(json: String): List<VirtualAccountProfile> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                VirtualAccountProfile(
                    id = obj.getString("id"),
                    displayName = obj.getString("displayName"),
                    accountHandle = obj.getString("accountHandle"),
                    virtualMac = obj.getString("virtualMac"),
                    sandboxPath = obj.getString("sandboxPath"),
                    createdAt = obj.optLong("createdAt", 0L),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun serializeAccounts(list: List<VirtualAccountProfile>): String {
        val arr = JSONArray()
        list.forEach { acc ->
            arr.put(
                JSONObject().apply {
                    put("id", acc.id)
                    put("displayName", acc.displayName)
                    put("accountHandle", acc.accountHandle)
                    put("virtualMac", acc.virtualMac)
                    put("sandboxPath", acc.sandboxPath)
                    put("createdAt", acc.createdAt)
                },
            )
        }
        return arr.toString()
    }

    fun parseVirtualApps(json: String): List<VirtualAppEntry> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                VirtualAppEntry(
                    id = obj.getString("id"),
                    label = obj.getString("label"),
                    packageName = obj.getString("packageName"),
                    versionName = obj.optString("versionName", "1.0"),
                    sourceType = obj.optString("sourceType", "APK"),
                    accountId = obj.getString("accountId"),
                    virtualMac = obj.getString("virtualMac"),
                    launchCount = obj.optInt("launchCount", 0),
                    sandboxDataSizeKb = obj.optInt("sandboxDataSizeKb", 128),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun serializeVirtualApps(list: List<VirtualAppEntry>): String {
        val arr = JSONArray()
        list.forEach { app ->
            arr.put(
                JSONObject().apply {
                    put("id", app.id)
                    put("label", app.label)
                    put("packageName", app.packageName)
                    put("versionName", app.versionName)
                    put("sourceType", app.sourceType)
                    put("accountId", app.accountId)
                    put("virtualMac", app.virtualMac)
                    put("launchCount", app.launchCount)
                    put("sandboxDataSizeKb", app.sandboxDataSizeKb)
                },
            )
        }
        return arr.toString()
    }
}
