package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.osDataStore: DataStore<Preferences> by preferencesDataStore(name = "miui_os_prefs")

data class OsPreferences(
    val setupCompleted: Boolean = false,
    val brightnessLevel: Float = 0.78f,
    val autoBrightness: Boolean = true,
    val readingMode: Boolean = false,
    val autoRotate: Boolean = true,
    val muteEnabled: Boolean = false,
    val gpsEnabled: Boolean = true,
    val mobileDataEnabled: Boolean = false,
    val airplaneModeEnabled: Boolean = false,
    val ram256Enabled: Boolean = true,
    val bgOptimizationEnabled: Boolean = true,
    val cpu15GhzBoostEnabled: Boolean = true,
    val customWallpaperUri: String? = null,
    // Battery 80% / 70% anti-drain & calm mode
    val batteryCalmAutoEnabled: Boolean = true,
    val batteryCalmThreshold: Int = 80, // 80 or 70
    val batteryCalmManualOverride: Boolean = false,
    // Separate 8 GB virtual RAM expansion
    val virtualRam8GbEnabled: Boolean = true,
    // Call screening & unknown caller prompt
    val callFilterEnabled: Boolean = true,
    val allowedCallNumbers: Set<String> = emptySet(),
    val blockedCallNumbers: Set<String> = emptySet(),
    val callHistoryJson: String = "[]",
    // Virtual environment: second accounts, virtual MACs & isolated APK/app entries
    val virtualAccountsJson: String = "",
    val activeVirtualAccountId: String = "",
    val virtualAppsJson: String = "[]",
)

class OsPreferencesRepository(private val context: Context) {

    private object Keys {
        val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val BRIGHTNESS_LEVEL = floatPreferencesKey("brightness_level")
        val AUTO_BRIGHTNESS = booleanPreferencesKey("auto_brightness")
        val READING_MODE = booleanPreferencesKey("reading_mode")
        val AUTO_ROTATE = booleanPreferencesKey("auto_rotate")
        val MUTE_ENABLED = booleanPreferencesKey("mute_enabled")
        val GPS_ENABLED = booleanPreferencesKey("gps_enabled")
        val MOBILE_DATA_ENABLED = booleanPreferencesKey("mobile_data_enabled")
        val AIRPLANE_MODE = booleanPreferencesKey("airplane_mode")
        val RAM_256_ENABLED = booleanPreferencesKey("ram_256_enabled")
        val BG_OPT_ENABLED = booleanPreferencesKey("bg_opt_enabled")
        val CPU_15GHZ_BOOST = booleanPreferencesKey("cpu_15ghz_boost")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")

        val BATTERY_CALM_AUTO = booleanPreferencesKey("battery_calm_auto")
        val BATTERY_CALM_THRESHOLD = intPreferencesKey("battery_calm_threshold")
        val BATTERY_CALM_MANUAL = booleanPreferencesKey("battery_calm_manual")
        val VIRTUAL_RAM_8GB = booleanPreferencesKey("virtual_ram_8gb")

        val CALL_FILTER_ENABLED = booleanPreferencesKey("call_filter_enabled")
        val ALLOWED_NUMBERS = stringSetPreferencesKey("allowed_call_numbers")
        val BLOCKED_NUMBERS = stringSetPreferencesKey("blocked_call_numbers")
        val CALL_HISTORY_JSON = stringPreferencesKey("call_history_json")

        val VIRTUAL_ACCOUNTS_JSON = stringPreferencesKey("virtual_accounts_json")
        val ACTIVE_VIRTUAL_ACCOUNT_ID = stringPreferencesKey("active_virtual_account_id")
        val VIRTUAL_APPS_JSON = stringPreferencesKey("virtual_apps_json")
    }

    val preferencesFlow: Flow<OsPreferences> = context.osDataStore.data.map { prefs ->
        OsPreferences(
            setupCompleted = prefs[Keys.SETUP_COMPLETED] ?: false,
            brightnessLevel = prefs[Keys.BRIGHTNESS_LEVEL] ?: 0.78f,
            autoBrightness = prefs[Keys.AUTO_BRIGHTNESS] ?: true,
            readingMode = prefs[Keys.READING_MODE] ?: false,
            autoRotate = prefs[Keys.AUTO_ROTATE] ?: true,
            muteEnabled = prefs[Keys.MUTE_ENABLED] ?: false,
            gpsEnabled = prefs[Keys.GPS_ENABLED] ?: true,
            mobileDataEnabled = prefs[Keys.MOBILE_DATA_ENABLED] ?: false,
            airplaneModeEnabled = prefs[Keys.AIRPLANE_MODE] ?: false,
            ram256Enabled = prefs[Keys.RAM_256_ENABLED] ?: true,
            bgOptimizationEnabled = prefs[Keys.BG_OPT_ENABLED] ?: true,
            cpu15GhzBoostEnabled = prefs[Keys.CPU_15GHZ_BOOST] ?: true,
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI],
            batteryCalmAutoEnabled = prefs[Keys.BATTERY_CALM_AUTO] ?: true,
            batteryCalmThreshold = prefs[Keys.BATTERY_CALM_THRESHOLD] ?: 80,
            batteryCalmManualOverride = prefs[Keys.BATTERY_CALM_MANUAL] ?: false,
            virtualRam8GbEnabled = prefs[Keys.VIRTUAL_RAM_8GB] ?: true,
            callFilterEnabled = prefs[Keys.CALL_FILTER_ENABLED] ?: true,
            allowedCallNumbers = prefs[Keys.ALLOWED_NUMBERS] ?: emptySet(),
            blockedCallNumbers = prefs[Keys.BLOCKED_NUMBERS] ?: emptySet(),
            callHistoryJson = prefs[Keys.CALL_HISTORY_JSON] ?: "[]",
            virtualAccountsJson = prefs[Keys.VIRTUAL_ACCOUNTS_JSON] ?: "",
            activeVirtualAccountId = prefs[Keys.ACTIVE_VIRTUAL_ACCOUNT_ID] ?: "",
            virtualAppsJson = prefs[Keys.VIRTUAL_APPS_JSON] ?: "[]",
        )
    }

    suspend fun setSetupCompleted(completed: Boolean) {
        context.osDataStore.edit { it[Keys.SETUP_COMPLETED] = completed }
    }

    suspend fun setBrightness(level: Float) {
        context.osDataStore.edit { it[Keys.BRIGHTNESS_LEVEL] = level.coerceIn(0.15f, 1.0f) }
    }

    suspend fun toggleAutoBrightness() {
        context.osDataStore.edit { it[Keys.AUTO_BRIGHTNESS] = !(it[Keys.AUTO_BRIGHTNESS] ?: true) }
    }

    suspend fun toggleReadingMode() {
        context.osDataStore.edit { it[Keys.READING_MODE] = !(it[Keys.READING_MODE] ?: false) }
    }

    suspend fun toggleAutoRotate() {
        context.osDataStore.edit { it[Keys.AUTO_ROTATE] = !(it[Keys.AUTO_ROTATE] ?: true) }
    }

    suspend fun toggleMute() {
        context.osDataStore.edit { it[Keys.MUTE_ENABLED] = !(it[Keys.MUTE_ENABLED] ?: false) }
    }

    suspend fun toggleGps() {
        context.osDataStore.edit { it[Keys.GPS_ENABLED] = !(it[Keys.GPS_ENABLED] ?: true) }
    }

    suspend fun toggleMobileData() {
        context.osDataStore.edit { it[Keys.MOBILE_DATA_ENABLED] = !(it[Keys.MOBILE_DATA_ENABLED] ?: false) }
    }

    suspend fun toggleAirplaneMode() {
        context.osDataStore.edit { it[Keys.AIRPLANE_MODE] = !(it[Keys.AIRPLANE_MODE] ?: false) }
    }

    suspend fun toggleRam256() {
        context.osDataStore.edit { it[Keys.RAM_256_ENABLED] = !(it[Keys.RAM_256_ENABLED] ?: true) }
    }

    suspend fun toggleBgOptimization() {
        context.osDataStore.edit { it[Keys.BG_OPT_ENABLED] = !(it[Keys.BG_OPT_ENABLED] ?: true) }
    }

    suspend fun toggleCpuBoost() {
        context.osDataStore.edit { it[Keys.CPU_15GHZ_BOOST] = !(it[Keys.CPU_15GHZ_BOOST] ?: true) }
    }

    suspend fun setCustomWallpaperUri(uri: String?) {
        context.osDataStore.edit {
            if (uri == null) it.remove(Keys.CUSTOM_WALLPAPER_URI)
            else it[Keys.CUSTOM_WALLPAPER_URI] = uri
        }
    }

    suspend fun toggleBatteryCalmAuto() {
        context.osDataStore.edit { it[Keys.BATTERY_CALM_AUTO] = !(it[Keys.BATTERY_CALM_AUTO] ?: true) }
    }

    suspend fun setBatteryCalmThreshold(percent: Int) {
        val normalized = if (percent == 70) 70 else 80
        context.osDataStore.edit { it[Keys.BATTERY_CALM_THRESHOLD] = normalized }
    }

    suspend fun toggleBatteryCalmManual() {
        context.osDataStore.edit { it[Keys.BATTERY_CALM_MANUAL] = !(it[Keys.BATTERY_CALM_MANUAL] ?: false) }
    }

    suspend fun toggleVirtualRam8Gb() {
        context.osDataStore.edit { it[Keys.VIRTUAL_RAM_8GB] = !(it[Keys.VIRTUAL_RAM_8GB] ?: true) }
    }

    suspend fun toggleCallFilter() {
        context.osDataStore.edit { it[Keys.CALL_FILTER_ENABLED] = !(it[Keys.CALL_FILTER_ENABLED] ?: true) }
    }

    suspend fun allowCallNumber(normalizedNumber: String) {
        context.osDataStore.edit { prefs ->
            val allowed = (prefs[Keys.ALLOWED_NUMBERS] ?: emptySet()).toMutableSet()
            val blocked = (prefs[Keys.BLOCKED_NUMBERS] ?: emptySet()).toMutableSet()
            allowed.add(normalizedNumber)
            blocked.remove(normalizedNumber)
            prefs[Keys.ALLOWED_NUMBERS] = allowed
            prefs[Keys.BLOCKED_NUMBERS] = blocked
        }
    }

    suspend fun blockCallNumber(normalizedNumber: String) {
        context.osDataStore.edit { prefs ->
            val allowed = (prefs[Keys.ALLOWED_NUMBERS] ?: emptySet()).toMutableSet()
            val blocked = (prefs[Keys.BLOCKED_NUMBERS] ?: emptySet()).toMutableSet()
            blocked.add(normalizedNumber)
            allowed.remove(normalizedNumber)
            prefs[Keys.ALLOWED_NUMBERS] = allowed
            prefs[Keys.BLOCKED_NUMBERS] = blocked
        }
    }

    suspend fun removeCallRule(normalizedNumber: String) {
        context.osDataStore.edit { prefs ->
            val allowed = (prefs[Keys.ALLOWED_NUMBERS] ?: emptySet()).toMutableSet()
            val blocked = (prefs[Keys.BLOCKED_NUMBERS] ?: emptySet()).toMutableSet()
            allowed.remove(normalizedNumber)
            blocked.remove(normalizedNumber)
            prefs[Keys.ALLOWED_NUMBERS] = allowed
            prefs[Keys.BLOCKED_NUMBERS] = blocked
        }
    }

    suspend fun setCallHistoryJson(json: String) {
        context.osDataStore.edit { it[Keys.CALL_HISTORY_JSON] = json }
    }

    suspend fun saveVirtualAccounts(accountsJson: String, activeId: String) {
        context.osDataStore.edit {
            it[Keys.VIRTUAL_ACCOUNTS_JSON] = accountsJson
            it[Keys.ACTIVE_VIRTUAL_ACCOUNT_ID] = activeId
        }
    }

    suspend fun saveVirtualAppsJson(json: String) {
        context.osDataStore.edit { it[Keys.VIRTUAL_APPS_JSON] = json }
    }
}
