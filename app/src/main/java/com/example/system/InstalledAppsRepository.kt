package com.example.system

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap?,
)

class InstalledAppsRepository(private val context: Context) {

    suspend fun loadLaunchableApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
        val selfPkg = context.packageName
        resolved
            .asSequence()
            .filter { it.activityInfo.packageName != selfPkg }
            .map { info ->
                val label = info.loadLabel(pm)?.toString().orEmpty().ifBlank { info.activityInfo.packageName }
                val iconBitmap = runCatching {
                    drawableToImageBitmap(info.loadIcon(pm))
                }.getOrNull()
                InstalledAppInfo(
                    label = label,
                    packageName = info.activityInfo.packageName,
                    activityName = info.activityInfo.name,
                    icon = iconBitmap,
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun launchApp(app: InstalledAppInfo): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun launchSystemDialer(): Boolean = startSafeIntent(Intent(Intent.ACTION_DIAL))

    fun launchSystemMessages(): Boolean = startSafeIntent(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)
    )

    fun launchSystemBrowser(): Boolean = startSafeIntent(
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
    )

    fun launchSystemCamera(): Boolean = startSafeIntent(
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
    )

    fun launchSystemMusic(): Boolean = startSafeIntent(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)
    )

    fun launchPlayStore(): Boolean = startSafeIntent(
        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store"))
    )

    fun openAndroidWifiSettings(): Boolean = startSafeIntent(Intent(Settings.ACTION_WIFI_SETTINGS))

    fun openAndroidBluetoothSettings(): Boolean = startSafeIntent(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))

    fun openAndroidWirelessSettings(): Boolean = startSafeIntent(Intent(Settings.ACTION_WIRELESS_SETTINGS))

    fun openAndroidDataUsageSettings(): Boolean = startSafeIntent(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))

    fun openHomeLauncherSettings(): Boolean = startSafeIntent(Intent(Settings.ACTION_HOME_SETTINGS))

    fun createDefaultHomeRoleIntent(): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null &&
                roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }

    fun isDefaultHomeHeld(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        return false
    }

    private fun startSafeIntent(intent: Intent): Boolean {
        return runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap.asImageBitmap()
        }
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 128
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 128
        val bitmap = Bitmap.createBitmap(width.coerceAtMost(192), height.coerceAtMost(192), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }
}
