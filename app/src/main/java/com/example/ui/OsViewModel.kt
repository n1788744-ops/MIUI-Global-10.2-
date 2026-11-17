package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.OsPreferences
import com.example.data.OsPreferencesRepository
import com.example.system.CallFilterEventBus
import com.example.system.CallFilterManager
import com.example.system.CallScreenActionType
import com.example.system.InstalledAppInfo
import com.example.system.InstalledAppsRepository
import com.example.system.PendingUnknownCallPrompt
import com.example.system.ScreenedCallRecord
import com.example.system.SystemTelemetry
import com.example.system.SystemTelemetryEngine
import com.example.system.VirtualAccountProfile
import com.example.system.VirtualAppEntry
import com.example.system.VirtualRam8GbEngine
import com.example.system.VirtualRam8GbTelemetry
import com.example.system.VirtualSpaceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
    data object VirtualSpace : OsRoute
    data object CallScreening : OsRoute
    data object BatteryCalmMode : OsRoute
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
    val virtualRam8Gb: VirtualRam8GbTelemetry = VirtualRam8GbTelemetry(),
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
    // Virtual environment state
    val virtualAccounts: List<VirtualAccountProfile> = emptyList(),
    val activeVirtualAccount: VirtualAccountProfile? = null,
    val virtualApps: List<VirtualAppEntry> = emptyList(),
    val activeVirtualSessionApp: VirtualAppEntry? = null,
    // Call screening state
    val isCallScreeningRoleHeld: Boolean = false,
    val hasContactsPermission: Boolean = false,
    val callHistory: List<ScreenedCallRecord> = emptyList(),
    val pendingUnknownPrompt: PendingUnknownCallPrompt? = null,
) {
    val currentRoute: OsRoute get() = routeStack.lastOrNull() ?: OsRoute.Home

    val isBatteryCalmActive: Boolean
        get() = preferences.batteryCalmManualOverride ||
            (preferences.batteryCalmAutoEnabled && telemetry.batteryPercent <= preferences.batteryCalmThreshold)
}

class OsViewModel(application: Application) : AndroidViewModel(application) {

    private val telemetryEngine = SystemTelemetryEngine(application)
    private val virtualRam8GbEngine = VirtualRam8GbEngine(application)
    private val virtualSpaceManager = VirtualSpaceManager(application)
    private val callFilterManager = CallFilterManager(application)
    private val appsRepository = InstalledAppsRepository(application)
    private val prefsRepository = OsPreferencesRepository(application)

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    private val _virtualRam8Gb = MutableStateFlow(VirtualRam8GbTelemetry())
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

    private val _isCallRoleHeld = MutableStateFlow(false)
    private val _hasContactsPermission = MutableStateFlow(false)
    private val _pendingUnknownPrompt = MutableStateFlow<PendingUnknownCallPrompt?>(null)
    private val _activeVirtualSessionApp = MutableStateFlow<VirtualAppEntry?>(null)

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

    private val _extraFlags = combine(
        _virtualRam8Gb,
        _isCallRoleHeld,
        _hasContactsPermission,
        _pendingUnknownPrompt,
        _activeVirtualSessionApp,
    ) { vram8, callRole, contactsPerm, unknownPrompt, activeVApp ->
        ExtraFlags(vram8, callRole, contactsPerm, unknownPrompt, activeVApp)
    }

    val uiState: StateFlow<OsUiState> = combine(
        _prefsCache,
        _telemetry,
        _installedApps,
        _clock,
        _routeStack,
        _uiFlags,
        _setupFlags,
        _extraFlags,
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
        val extra = values[7] as ExtraFlags

        val vAccounts = virtualSpaceManager.parseAccounts(prefs.virtualAccountsJson)
        val activeAccount = vAccounts.firstOrNull { it.id == prefs.activeVirtualAccountId }
            ?: vAccounts.firstOrNull()
        val vApps = virtualSpaceManager.parseVirtualApps(prefs.virtualAppsJson)
        val callHist = callFilterManager.parseHistory(prefs.callHistoryJson)

        OsUiState(
            isPreferencesLoaded = setup.isPrefsLoaded,
            preferences = prefs,
            telemetry = tel,
            virtualRam8Gb = extra.virtualRam8Gb,
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
            virtualAccounts = vAccounts,
            activeVirtualAccount = activeAccount,
            virtualApps = vApps,
            activeVirtualSessionApp = extra.activeVirtualSessionApp,
            isCallScreeningRoleHeld = extra.isCallRoleHeld,
            hasContactsPermission = extra.hasContactsPermission,
            callHistory = callHist,
            pendingUnknownPrompt = extra.pendingUnknownPrompt,
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

                // Ensure at least one Virtual Account with a unique Virtual MAC exists
                if (prefs.virtualAccountsJson.isBlank()) {
                    val (defaults, activeId) = virtualSpaceManager.createDefaultAccountIfEmpty("")
                    prefsRepository.saveVirtualAccounts(
                        virtualSpaceManager.serializeAccounts(defaults),
                        activeId,
                    )
                }
            }
        }
        viewModelScope.launch {
            _installedApps.value = appsRepository.loadLaunchableApps()
            refreshSystemRolesAndPermissions()
            _virtualRam8Gb.value = virtualRam8GbEngine.allocateOrVerifyPool(
                _prefsCache.value.virtualRam8GbEnabled,
            )
        }
        viewModelScope.launch {
            CallFilterEventBus.pendingUnknown.collect { prompt ->
                _pendingUnknownPrompt.value = prompt
            }
        }
        viewModelScope.launch {
            while (isActive) {
                _clock.value = readClockSnapshot()
                _telemetry.value = telemetryEngine.collectTelemetry()
                refreshSystemRolesAndPermissions()
                val currentPrefs = _prefsCache.value
                val calmActive = currentPrefs.batteryCalmManualOverride ||
                    (currentPrefs.batteryCalmAutoEnabled && _telemetry.value.batteryPercent <= currentPrefs.batteryCalmThreshold)
                // Throttle telemetry polling when 80%/70% Battery Calm Mode is active to reduce drain
                delay(if (calmActive) 5000L else 2000L)
            }
        }
        // Periodic background optimization loop using dedicated background optimization memory
        viewModelScope.launch {
            while (isActive) {
                delay(25_000L)
                val prefs = _prefsCache.value
                if (prefs.bgOptimizationEnabled) {
                    _telemetry.value = telemetryEngine.runBackgroundOptimizationSweep()
                    if (prefs.virtualRam8GbEnabled) {
                        _virtualRam8Gb.value = virtualRam8GbEngine.allocateOrVerifyPool(true)
                    }
                }
            }
        }
    }

    fun refreshSystemRolesAndPermissions() {
        _isDefaultHome.value = appsRepository.isDefaultHomeHeld()
        _isCallRoleHeld.value = callFilterManager.isCallScreeningRoleHeld()
        _hasContactsPermission.value = callFilterManager.hasContactsPermission()
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
            _setupProgress.value = 0.45f
            _installedApps.value = appsRepository.loadLaunchableApps()
            _virtualRam8Gb.value = virtualRam8GbEngine.allocateOrVerifyPool(true)
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
        refreshSystemRolesAndPermissions()
    }

    fun createDefaultHomeIntent(): Intent? = appsRepository.createDefaultHomeRoleIntent()

    fun createCallScreeningRoleIntent(): Intent? = callFilterManager.createCallScreeningRoleIntent()

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
        if (_activeVirtualSessionApp.value != null) {
            _activeVirtualSessionApp.value = null
            return true
        }
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
            if (_prefsCache.value.virtualRam8GbEnabled) {
                _virtualRam8Gb.value = virtualRam8GbEngine.expandAndCompactSwapPages()
            }
            _isOptimizing.value = false
            _messages.tryEmit(
                "Оптимизировано: освобождено ${updated.lastOptimizationFreedMb} МБ (256 МБ ОЗУ + 8 ГБ вирт. пул)",
            )
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

    fun toggleVirtualRam8Gb() {
        viewModelScope.launch {
            prefsRepository.toggleVirtualRam8Gb()
            val nextEnabled = !_prefsCache.value.virtualRam8GbEnabled
            _virtualRam8Gb.value = virtualRam8GbEngine.allocateOrVerifyPool(nextEnabled)
        }
    }

    fun toggleBatteryCalmAuto() {
        viewModelScope.launch { prefsRepository.toggleBatteryCalmAuto() }
    }

    fun setBatteryCalmThreshold(threshold: Int) {
        viewModelScope.launch {
            prefsRepository.setBatteryCalmThreshold(threshold)
            _messages.tryEmit("Порог режима покоя и экономии: $threshold%")
        }
    }

    fun toggleBatteryCalmManual() {
        viewModelScope.launch { prefsRepository.toggleBatteryCalmManual() }
    }

    fun setCustomWallpaperUri(uri: String?) {
        viewModelScope.launch { prefsRepository.setCustomWallpaperUri(uri) }
    }

    // --- Virtual Environment (Second Account + Virtual MAC + Isolated APK/App) ---

    fun createVirtualAccount(displayName: String, accountHandle: String) {
        viewModelScope.launch {
            val newAcc = virtualSpaceManager.createAccount(displayName, accountHandle)
            val existing = virtualSpaceManager.parseAccounts(_prefsCache.value.virtualAccountsJson)
            val updated = existing + newAcc
            prefsRepository.saveVirtualAccounts(
                virtualSpaceManager.serializeAccounts(updated),
                newAcc.id,
            )
            _messages.tryEmit("Создан второй аккаунт: ${newAcc.displayName} (MAC: ${newAcc.virtualMac})")
        }
    }

    fun selectVirtualAccount(accountId: String) {
        viewModelScope.launch {
            val accounts = virtualSpaceManager.parseAccounts(_prefsCache.value.virtualAccountsJson)
            prefsRepository.saveVirtualAccounts(
                virtualSpaceManager.serializeAccounts(accounts),
                accountId,
            )
        }
    }

    fun regenerateActiveVirtualMac(customMac: String? = null) {
        viewModelScope.launch {
            val accounts = virtualSpaceManager.parseAccounts(_prefsCache.value.virtualAccountsJson)
            val activeId = _prefsCache.value.activeVirtualAccountId.ifBlank { accounts.firstOrNull()?.id.orEmpty() }
            val nextMac = if (!customMac.isNullOrBlank() && virtualSpaceManager.isValidMacAddress(customMac)) {
                customMac.trim().uppercase(Locale.US)
            } else {
                virtualSpaceManager.generateLocallyAdministeredMac()
            }
            val updated = accounts.map { acc ->
                if (acc.id == activeId) acc.copy(virtualMac = nextMac) else acc
            }
            prefsRepository.saveVirtualAccounts(
                virtualSpaceManager.serializeAccounts(updated),
                activeId,
            )
            val apps = virtualSpaceManager.parseVirtualApps(_prefsCache.value.virtualAppsJson).map { app ->
                if (app.accountId == activeId) app.copy(virtualMac = nextMac) else app
            }
            prefsRepository.saveVirtualAppsJson(virtualSpaceManager.serializeVirtualApps(apps))
            _messages.tryEmit("Новый MAC-адрес виртуальной среды: $nextMac")
        }
    }

    fun importApkToVirtualSpace(uri: Uri) {
        viewModelScope.launch {
            val accounts = virtualSpaceManager.parseAccounts(_prefsCache.value.virtualAccountsJson)
            val activeAcc = accounts.firstOrNull { it.id == _prefsCache.value.activeVirtualAccountId }
                ?: accounts.firstOrNull()
                ?: return@launch

            virtualSpaceManager.importApkFromUri(uri, activeAcc)
                .onSuccess { entry ->
                    val existingApps = virtualSpaceManager.parseVirtualApps(_prefsCache.value.virtualAppsJson)
                    prefsRepository.saveVirtualAppsJson(
                        virtualSpaceManager.serializeVirtualApps(listOf(entry) + existingApps),
                    )
                    _messages.tryEmit("APK «${entry.label}» добавлен в виртуальную среду (${activeAcc.displayName})")
                }
                .onFailure {
                    _messages.tryEmit("Не удалось импортировать APK: ${it.message ?: "ошибка файла"}")
                }
        }
    }

    fun cloneInstalledAppToVirtualSpace(app: InstalledAppInfo) {
        viewModelScope.launch {
            val accounts = virtualSpaceManager.parseAccounts(_prefsCache.value.virtualAccountsJson)
            val activeAcc = accounts.firstOrNull { it.id == _prefsCache.value.activeVirtualAccountId }
                ?: accounts.firstOrNull()
                ?: return@launch

            val cloned = virtualSpaceManager.cloneInstalledApp(app, activeAcc)
            val existingApps = virtualSpaceManager.parseVirtualApps(_prefsCache.value.virtualAppsJson)
            prefsRepository.saveVirtualAppsJson(
                virtualSpaceManager.serializeVirtualApps(listOf(cloned) + existingApps),
            )
            _messages.tryEmit("«${app.label}» клонировано во второй аккаунт (${activeAcc.virtualMac})")
        }
    }

    fun launchVirtualApp(entry: VirtualAppEntry) {
        viewModelScope.launch {
            val existingApps = virtualSpaceManager.parseVirtualApps(_prefsCache.value.virtualAppsJson)
            val updated = existingApps.map {
                if (it.id == entry.id) it.copy(launchCount = it.launchCount + 1) else it
            }
            prefsRepository.saveVirtualAppsJson(virtualSpaceManager.serializeVirtualApps(updated))
            _activeVirtualSessionApp.value = entry.copy(launchCount = entry.launchCount + 1)
        }
    }

    fun launchRealPackageFromVirtualSession(packageName: String) {
        val target = _installedApps.value.firstOrNull { it.packageName == packageName }
        if (target != null) {
            appsRepository.launchApp(target)
        } else {
            _messages.tryEmit("APK изолирован внутри виртуального контейнера")
        }
    }

    fun closeVirtualSession() {
        _activeVirtualSessionApp.value = null
    }

    fun removeVirtualApp(entryId: String) {
        viewModelScope.launch {
            val existingApps = virtualSpaceManager.parseVirtualApps(_prefsCache.value.virtualAppsJson)
            prefsRepository.saveVirtualAppsJson(
                virtualSpaceManager.serializeVirtualApps(existingApps.filterNot { it.id == entryId }),
            )
        }
    }

    // --- Call Screening (Contacts pass, Unknown sends Allow/Reject prompt) ---

    fun toggleCallFilter() {
        viewModelScope.launch { prefsRepository.toggleCallFilter() }
    }

    fun screenIncomingNumberCheck(rawNumber: String) {
        if (rawNumber.isBlank()) return
        viewModelScope.launch {
            val prefs = _prefsCache.value
            val (decision, contactName) = callFilterManager.evaluateNumber(rawNumber, prefs)
            val normalized = callFilterManager.normalizePhoneNumber(rawNumber)
            val record = ScreenedCallRecord(
                id = UUID.randomUUID().toString(),
                rawNumber = rawNumber.trim(),
                normalizedNumber = normalized,
                contactName = contactName,
                status = decision,
                timestampMs = System.currentTimeMillis(),
            )
            val existing = callFilterManager.parseHistory(prefs.callHistoryJson)
            prefsRepository.setCallHistoryJson(
                callFilterManager.serializeHistory(listOf(record) + existing),
            )

            when (decision) {
                CallScreenActionType.ALLOWED_CONTACT -> {
                    _messages.tryEmit("Контакт «$contactName» — звонок пропущен")
                }
                CallScreenActionType.ALLOWED_BY_USER -> {
                    _messages.tryEmit("Номер $rawNumber в списке разрешённых — пропущен")
                }
                CallScreenActionType.REJECTED_BY_USER -> {
                    _messages.tryEmit("Номер $rawNumber отклонён фильтром")
                }
                CallScreenActionType.PENDING_UNKNOWN -> {
                    callFilterManager.postUnknownCallerNotification(rawNumber.trim(), normalized)
                    _pendingUnknownPrompt.value = PendingUnknownCallPrompt(
                        id = record.id,
                        rawNumber = rawNumber.trim(),
                        normalizedNumber = normalized,
                        timestampMs = record.timestampMs,
                    )
                }
            }
        }
    }

    fun resolveUnknownCall(normalizedNumber: String, allow: Boolean) {
        viewModelScope.launch {
            if (allow) {
                prefsRepository.allowCallNumber(normalizedNumber)
            } else {
                prefsRepository.blockCallNumber(normalizedNumber)
            }
            val existing = callFilterManager.parseHistory(_prefsCache.value.callHistoryJson)
            val updated = existing.map { rec ->
                if (rec.normalizedNumber == normalizedNumber && rec.status == CallScreenActionType.PENDING_UNKNOWN) {
                    rec.copy(
                        status = if (allow) CallScreenActionType.ALLOWED_BY_USER else CallScreenActionType.REJECTED_BY_USER,
                    )
                } else {
                    rec
                }
            }
            prefsRepository.setCallHistoryJson(callFilterManager.serializeHistory(updated))
            if (_pendingUnknownPrompt.value?.normalizedNumber == normalizedNumber) {
                _pendingUnknownPrompt.value = null
            }
            _messages.tryEmit(
                if (allow) "Звонок $normalizedNumber разрешён" else "Звонок $normalizedNumber отклонён",
            )
        }
    }

    fun removeCallRule(normalizedNumber: String) {
        viewModelScope.launch { prefsRepository.removeCallRule(normalizedNumber) }
    }

    // --- System App Launchers ---

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

    private data class ExtraFlags(
        val virtualRam8Gb: VirtualRam8GbTelemetry,
        val isCallRoleHeld: Boolean,
        val hasContactsPermission: Boolean,
        val pendingUnknownPrompt: PendingUnknownCallPrompt?,
        val activeVirtualSessionApp: VirtualAppEntry?,
    )
}
