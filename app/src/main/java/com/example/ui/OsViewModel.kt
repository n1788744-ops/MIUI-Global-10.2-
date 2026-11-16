package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.OsPreferences
import com.example.data.OsPreferencesRepository
import com.example.system.InstalledAppInfo
import com.example.system.InstalledAppsRepository
import com.example.system.SystemTelemetry
import com.example.system.SystemTelemetryEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface OsRoute {
    data object Home : OsRoute
    data object Settings : OsRoute
    data object AboutPhone : OsRoute
    data object SecurityCenter : OsRoute
    data object DisplaySettings : OsRoute
    data object PerformanceSettings : OsRoute
    data object LauncherAccess : OsRoute
    data object Calculator : OsRoute
    data object Compass : OsRoute
}

enum class DesktopFolder(val title: String) {
    GOOGLE("Google"),
    TOOLS("Инструменты"),
}

data class ClockSnapshot(
    val timeText: String = "16:51",
    val fullDateText: String = "Понедельник, 16 ноября",
    val shortDateText: String = "Пн, 16 нояб.",
)

data class OsUiState(
    val isPreferencesLoaded: Boolean = false,
    val preferences: OsPreferences = OsPreferences(),
    val telemetry: SystemTelemetry = SystemTelemetry(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val clock: ClockSnapshot = ClockSnapshot(),
    val routeStack: List<OsRoute> = listOf(OsRoute.Home),
    val isShadeOpen: Boolean = false,
    val isLocked: Boolean = false,
    val activeFolder: DesktopFolder? = null,
    val isSearchOpen: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val isOptimizing: Boolean = false,
    val isInitializingSetup: Boolean = false,
    val setupProgress: Float = 0f,
    val isDefaultHome: Boolean = false,
) {
    val currentRoute: OsRoute get() = routeStack.lastOrNull() ?: OsRoute.Home
}

class OsViewModel(application: Application) : AndroidViewModel(application) {

    private val telemetryEngine = SystemTelemetryEngine(application)
    private val appsRepository = InstalledAppsRepository(application)
    private val prefsRepository = OsPreferencesRepository(application)

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val _clock = MutableStateFlow(readClockSnapshot())
    private val _routeStack = MutableStateFlow<List<OsRoute>>(listOf(OsRoute.Home))
    private val _isShadeOpen = MutableStateFlow(false)
    private val _isLocked = MutableStateFlow(false)
    private val _activeFolder = MutableStateFlow<DesktopFolder?>(null)
    private val _isSearchOpen = MutableStateFlow(false)
    private val _isFlashlightOn = MutableStateFlow(false)
    private val _isOptimizing = MutableStateFlow(false)
    private val _isInitializingSetup = MutableStateFlow(false)
    private val _setupProgress = MutableStateFlow(0f)
    private val _isDefaultHome = MutableStateFlow(false)
    private val _isPrefsLoaded = MutableStateFlow(false)
    private val _prefsCache = MutableStateFlow(OsPreferences())

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _uiFlags = combine(
        _isShadeOpen,
        _isLocked,
        _activeFolder,
        _isSearchOpen,
        _isFlashlightOn,
    ) { shade, locked, folder, search, flash ->
        UiFlags(shade, locked, folder, search, flash)
    }

    private val _setupFlags = combine(
        _isOptimizing,
        _isInitializingSetup,
        _setupProgress,
        _isDefaultHome,
        _isPrefsLoaded,
    ) { opt, init, prog, defHome, loaded ->
        SetupFlags(opt, init, prog, defHome, loaded)
    }

    val uiState: StateFlow<OsUiState> = combine(
        _prefsCache,
        _telemetry,
        _installedApps,
        _clock,
        _routeStack,
        _uiFlags,
        _setupFlags,
    ) { values ->
        val prefs = values[0] as OsPreferences
        val tel = values[1] as SystemTelemetry
        @Suppress("UNCHECKED_CAST")
        val apps = values[2] as List<InstalledAppInfo>
        val clk = values[3] as ClockSnapshot
        @Suppress("UNCHECKED_CAST")
        val stack = values[4] as List<OsRoute>
        val flags = values[5] as UiFlags
        val setup = values[6] as SetupFlags

        OsUiState(
            isPreferencesLoaded = setup.isPrefsLoaded,
            preferences = prefs,
            telemetry = tel,
            installedApps = apps,
            clock = clk,
            routeStack = stack,
            isShadeOpen = flags.isShadeOpen,
            isLocked = flags.isLocked,
            activeFolder = flags.activeFolder,
            isSearchOpen = flags.isSearchOpen,
            isFlashlightOn = flags.isFlashlightOn,
            isOptimizing = setup.isOptimizing,
            isInitializingSetup = setup.isInitializingSetup,
            setupProgress = setup.setupProgress,
            isDefaultHome = setup.isDefaultHome,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = OsUiState(),
    )

    init {
        telemetryEngine.primeOptimizationMemoryPool()
        viewModelScope.launch {
            prefsRepository.preferencesFlow.collect { prefs ->
                _prefsCache.value = prefs
                _isPrefsLoaded.value = true
            }
        }
        viewModelScope.launch {
            _installedApps.value = appsRepository.loadLaunchableApps()
            _isDefaultHome.value = appsRepository.isDefaultHomeHeld()
        }
        viewModelScope.launch {
            while (isActive) {
                _clock.value = readClockSnapshot()
                _telemetry.value = telemetryEngine.collectTelemetry()
                _isDefaultHome.value = appsRepository.isDefaultHomeHeld()
                delay(2000L)
            }
        }
        // Periodic background optimization loop using dedicated background optimization memory
        viewModelScope.launch {
            while (isActive) {
                delay(25_000L)
                if (_prefsCache.value.bgOptimizationEnabled) {
                    _telemetry.value = telemetryEngine.runBackgroundOptimizationSweep()
                }
            }
        }
    }

    fun updateMeasuredFps(fps: Int) {
        telemetryEngine.updateMeasuredFps(fps)
    }

    fun completeFirstLaunchSetup() {
        if (_isInitializingSetup.value) return
        viewModelScope.launch {
            _isInitializingSetup.value = true
            _setupProgress.value = 0.15f
            telemetryEngine.primeOptimizationMemoryPool()
            delay(260L)
            _setupProgress.value = 0.50f
            _installedApps.value = appsRepository.loadLaunchableApps()
            delay(280L)
            _setupProgress.value = 0.85f
            _telemetry.value = telemetryEngine.runBackgroundOptimizationSweep()
            delay(260L)
            _setupProgress.value = 1.0f
            prefsRepository.setSetupCompleted(true)
            _isInitializingSetup.value = false
        }
    }

    fun refreshDefaultHomeStatus() {
        _isDefaultHome.value = appsRepository.isDefaultHomeHeld()
    }

    fun createDefaultHomeIntent(): Intent? = appsRepository.createDefaultHomeRoleIntent()

    fun openHomeSettings() {
        appsRepository.openHomeLauncherSettings()
    }

    fun navigateTo(route: OsRoute) {
        _isShadeOpen.value = false
        _activeFolder.value = null
        _isSearchOpen.value = false
        val current = _routeStack.value
        if (current.lastOrNull() != route) {
            _routeStack.value = current + route
        }
    }

    fun navigateBack(): Boolean {
        if (_isLocked.value) return true
        if (_activeFolder.value != null) {
            _activeFolder.value = null
            return true
        }
        if (_isSearchOpen.value) {
            _isSearchOpen.value = false
            return true
        }
        if (_isShadeOpen.value) {
            _isShadeOpen.value = false
            return true
        }
        val current = _routeStack.value
        return if (current.size > 1) {
            _routeStack.value = current.dropLast(1)
            true
        } else {
            false
        }
    }

    fun returnHome() {
        _isShadeOpen.value = false
        _activeFolder.value = null
        _isSearchOpen.value = false
        _routeStack.value = listOf(OsRoute.Home)
    }

    fun setShadeOpen(open: Boolean) {
        _isShadeOpen.value = open
    }

    fun openFolder(folder: DesktopFolder?) {
        _activeFolder.value = folder
    }

    fun setSearchOpen(open: Boolean) {
        _isSearchOpen.value = open
    }

    fun lockDevice() {
        _isShadeOpen.value = false
        _isLocked.value = true
    }

    fun unlockDevice() {
        _isLocked.value = false
    }

    fun runManualOptimization() {
        if (_isOptimizing.value) return
        viewModelScope.launch {
            _isOptimizing.value = true
            delay(650L)
            val updated = telemetryEngine.runBackgroundOptimizationSweep()
            _telemetry.value = updated
            _isOptimizing.value = false
            _messages.tryEmit("Оптимизировано: освобождено ${updated.lastOptimizationFreedMb} МБ (пул 256 МБ стабилен)")
        }
    }

    fun toggleFlashlight() {
        val next = !_isFlashlightOn.value
        val hwSuccess = telemetryEngine.setFlashlight(next)
        _isFlashlightOn.value = next
        if (!hwSuccess) {
            _messages.tryEmit(if (next) "Фонарик: режим яркого экрана" else "Фонарик выключен")
        }
    }

    fun setBrightness(value: Float) {
        viewModelScope.launch { prefsRepository.setBrightness(value) }
    }

    fun toggleAutoBrightness() {
        viewModelScope.launch { prefsRepository.toggleAutoBrightness() }
    }

    fun toggleReadingMode() {
        viewModelScope.launch { prefsRepository.toggleReadingMode() }
    }

    fun toggleAutoRotate() {
        viewModelScope.launch { prefsRepository.toggleAutoRotate() }
    }

    fun toggleMute() {
        viewModelScope.launch { prefsRepository.toggleMute() }
    }

    fun toggleGps() {
        viewModelScope.launch { prefsRepository.toggleGps() }
    }

    fun toggleMobileData() {
        viewModelScope.launch { prefsRepository.toggleMobileData() }
    }

    fun toggleAirplaneMode() {
        viewModelScope.launch { prefsRepository.toggleAirplaneMode() }
    }

    fun toggleRam256Profile() {
        viewModelScope.launch { prefsRepository.toggleRam256() }
    }

    fun toggleBgOptimization() {
        viewModelScope.launch { prefsRepository.toggleBgOptimization() }
    }

    fun toggleCpuBoost() {
        viewModelScope.launch { prefsRepository.toggleCpuBoost() }
    }

    fun setCustomWallpaperUri(uri: String?) {
        viewModelScope.launch { prefsRepository.setCustomWallpaperUri(uri) }
    }

    fun launchInstalledApp(app: InstalledAppInfo) {
        _isSearchOpen.value = false
        _activeFolder.value = null
        if (!appsRepository.launchApp(app)) {
            _messages.tryEmit("Не удалось открыть ${app.label}")
        }
    }

    fun launchDialer() {
        if (!appsRepository.launchSystemDialer()) _messages.tryEmit("Телефон недоступен")
    }

    fun launchMessages() {
        if (!appsRepository.launchSystemMessages()) _messages.tryEmit("Сообщения недоступны")
    }

    fun launchBrowser() {
        if (!appsRepository.launchSystemBrowser()) _messages.tryEmit("Браузер недоступен")
    }

    fun launchCamera() {
        if (!appsRepository.launchSystemCamera()) _messages.tryEmit("Камера недоступна")
    }

    fun launchMusic() {
        if (!appsRepository.launchSystemMusic()) _messages.tryEmit("Музыкальный плеер не найден")
    }

    fun launchPlayStore() {
        if (!appsRepository.launchPlayStore()) _messages.tryEmit("Play Маркет не установлен")
    }

    fun openWifiSettings() {
        if (!appsRepository.openAndroidWifiSettings()) _messages.tryEmit("Настройки Wi-Fi недоступны")
    }

    fun openBluetoothSettings() {
        if (!appsRepository.openAndroidBluetoothSettings()) _messages.tryEmit("Настройки Bluetooth недоступны")
    }

    fun openWirelessSettings() {
        if (!appsRepository.openAndroidWirelessSettings()) _messages.tryEmit("Сетевые настройки недоступны")
    }

    fun openDataSettings() {
        if (!appsRepository.openAndroidDataUsageSettings()) _messages.tryEmit("Параметры передачи данных недоступны")
    }

    fun showMessage(msg: String) {
        _messages.tryEmit(msg)
    }

    private fun readClockSnapshot(): ClockSnapshot {
        val now = Date()
        val ru = Locale("ru", "RU")
        val timeText = SimpleDateFormat("HH:mm", ru).format(now)
        val fullDateRaw = SimpleDateFormat("EEEE, d MMMM", ru).format(now)
        val shortDateRaw = SimpleDateFormat("EE, d MMM", ru).format(now)
        return ClockSnapshot(
            timeText = timeText,
            fullDateText = fullDateRaw.replaceFirstChar { it.titlecase(ru) },
            shortDateText = shortDateRaw.replaceFirstChar { it.titlecase(ru) },
        )
    }

    private data class UiFlags(
        val isShadeOpen: Boolean,
        val isLocked: Boolean,
        val activeFolder: DesktopFolder?,
        val isSearchOpen: Boolean,
        val isFlashlightOn: Boolean,
    )

    private data class SetupFlags(
        val isOptimizing: Boolean,
        val isInitializingSetup: Boolean,
        val setupProgress: Float,
        val isDefaultHome: Boolean,
        val isPrefsLoaded: Boolean,
    )
}
