package com.example

import android.app.Activity
import android.os.Bundle
import android.view.Choreographer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.OsRoute
import com.example.ui.OsUiState
import com.example.ui.OsViewModel
import com.example.ui.components.MiuiWallpaperBackground
import com.example.ui.components.SystemDisplayOverlay
import com.example.ui.screens.AboutPhoneScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.CompassScreen
import com.example.ui.screens.ControlShadeOverlay
import com.example.ui.screens.DisplaySettingsScreen
import com.example.ui.screens.HomeDesktopScreen
import com.example.ui.screens.LauncherAccessScreen
import com.example.ui.screens.LockScreenOverlay
import com.example.ui.screens.PerformanceSettingsScreen
import com.example.ui.screens.SecurityCenterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupOnboardingScreen
import com.example.ui.theme.MiuiOsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: OsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiuiOsTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                val homeRoleLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                ) {
                    viewModel.refreshDefaultHomeStatus()
                }

                // Live UI smoothness / FPS measurement
                DisposableEffect(Unit) {
                    var frameCount = 0
                    var lastReportNs = System.nanoTime()
                    val choreographer = Choreographer.getInstance()
                    val callback = object : Choreographer.FrameCallback {
                        override fun doFrame(frameTimeNanos: Long) {
                            frameCount++
                            val elapsed = frameTimeNanos - lastReportNs
                            if (elapsed >= 1_000_000_000L) {
                                val fps = ((frameCount * 1_000_000_000.0) / elapsed).toInt()
                                viewModel.updateMeasuredFps(fps)
                                frameCount = 0
                                lastReportNs = frameTimeNanos
                            }
                            choreographer.postFrameCallback(this)
                        }
                    }
                    choreographer.postFrameCallback(callback)
                    onDispose { choreographer.removeFrameCallback(callback) }
                }

                // Apply brightness to current window when adjusted
                LaunchedEffect(uiState.preferences.brightnessLevel, uiState.preferences.autoBrightness) {
                    val act = context as? Activity ?: return@LaunchedEffect
                    val lp = act.window.attributes
                    lp.screenBrightness = if (uiState.preferences.autoBrightness) -1f else uiState.preferences.brightnessLevel
                    act.window.attributes = lp
                }

                LaunchedEffect(Unit) {
                    viewModel.messages.collect { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                MiuiOsRoot(
                    state = uiState,
                    viewModel = viewModel,
                    onRequestDefaultHome = {
                        val intent = viewModel.createDefaultHomeIntent()
                        if (intent != null) {
                            runCatching { homeRoleLauncher.launch(intent) }
                                .onFailure { viewModel.openHomeSettings() }
                        } else {
                            viewModel.openHomeSettings()
                        }
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDefaultHomeStatus()
    }
}

@Composable
private fun MiuiOsRoot(
    state: OsUiState,
    viewModel: OsViewModel,
    onRequestDefaultHome: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (!state.isPreferencesLoaded) {
            MiuiWallpaperBackground(customWallpaperUri = null)
        } else if (!state.preferences.setupCompleted) {
            SetupOnboardingScreen(
                telemetry = state.telemetry,
                isInitializing = state.isInitializingSetup,
                progress = state.setupProgress,
                isDefaultHome = state.isDefaultHome,
                onRequestDefaultHome = onRequestDefaultHome,
                onGrantAccessAndLaunch = { viewModel.completeFirstLaunchSetup() },
            )
        } else {
            // Persistent MIUI 10 Wallpaper behind desktop
            MiuiWallpaperBackground(customWallpaperUri = state.preferences.customWallpaperUri)

            HomeDesktopScreen(
                state = state,
                onOpenShade = { viewModel.setShadeOpen(true) },
                onNavigate = { viewModel.navigateTo(it) },
                onOpenFolder = { viewModel.openFolder(it) },
                onSetSearchOpen = { viewModel.setSearchOpen(it) },
                onLaunchInstalledApp = { viewModel.launchInstalledApp(it) },
                onLaunchBrowser = { viewModel.launchBrowser() },
                onLaunchMusic = { viewModel.launchMusic() },
                onLaunchPlayStore = { viewModel.launchPlayStore() },
                onLaunchDialer = { viewModel.launchDialer() },
                onLaunchMessages = { viewModel.launchMessages() },
                onLaunchCamera = { viewModel.launchCamera() },
                onToggleFlashlight = { viewModel.toggleFlashlight() },
                onPickWallpaper = { uri -> viewModel.setCustomWallpaperUri(uri?.toString()) },
                onShowToast = { viewModel.showMessage(it) },
            )

            // Active sub-screens (Settings, About Phone, Security Center, Display, Performance, Tools)
            AnimatedVisibility(
                visible = state.currentRoute != OsRoute.Home,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                when (state.currentRoute) {
                    OsRoute.Home -> Unit
                    OsRoute.Settings -> SettingsScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onNavigate = { viewModel.navigateTo(it) },
                        onOpenWifiSettings = { viewModel.openWifiSettings() },
                        onOpenBluetoothSettings = { viewModel.openBluetoothSettings() },
                        onOpenWirelessSettings = { viewModel.openWirelessSettings() },
                        onOpenDataSettings = { viewModel.openDataSettings() },
                    )
                    OsRoute.AboutPhone -> AboutPhoneScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                    )
                    OsRoute.SecurityCenter -> SecurityCenterScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onRunOptimization = { viewModel.runManualOptimization() },
                    )
                    OsRoute.DisplaySettings -> DisplaySettingsScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onBrightnessChange = { viewModel.setBrightness(it) },
                        onToggleAutoBrightness = { viewModel.toggleAutoBrightness() },
                        onToggleReadingMode = { viewModel.toggleReadingMode() },
                        onSetWallpaperUri = { uri -> viewModel.setCustomWallpaperUri(uri?.toString()) },
                    )
                    OsRoute.PerformanceSettings -> PerformanceSettingsScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onToggleRam256 = { viewModel.toggleRam256Profile() },
                        onToggleBgOpt = { viewModel.toggleBgOptimization() },
                        onToggleCpuBoost = { viewModel.toggleCpuBoost() },
                        onRunOptimization = { viewModel.runManualOptimization() },
                    )
                    OsRoute.LauncherAccess -> LauncherAccessScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onRequestDefaultHome = onRequestDefaultHome,
                        onOpenHomeSettings = { viewModel.openHomeSettings() },
                    )
                    OsRoute.Calculator -> CalculatorScreen(
                        onBack = { viewModel.navigateBack() },
                    )
                    OsRoute.Compass -> CompassScreen(
                        onBack = { viewModel.navigateBack() },
                    )
                }
            }

            // Control Center / Quick Settings Shade (slides down from top)
            AnimatedVisibility(
                visible = state.isShadeOpen,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            ) {
                ControlShadeOverlay(
                    state = state,
                    onCloseShade = { viewModel.setShadeOpen(false) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onToggleMobileData = { viewModel.toggleMobileData() },
                    onOpenWifiSettings = { viewModel.openWifiSettings() },
                    onToggleFlashlight = { viewModel.toggleFlashlight() },
                    onToggleMute = { viewModel.toggleMute() },
                    onTakeScreenshotNotice = { viewModel.showMessage("Снимок экрана сохранён") },
                    onOpenBluetoothSettings = { viewModel.openBluetoothSettings() },
                    onToggleAutoBrightness = { viewModel.toggleAutoBrightness() },
                    onToggleAirplaneMode = { viewModel.toggleAirplaneMode() },
                    onLockDevice = { viewModel.lockDevice() },
                    onToggleGps = { viewModel.toggleGps() },
                    onToggleAutoRotate = { viewModel.toggleAutoRotate() },
                    onToggleReadingMode = { viewModel.toggleReadingMode() },
                    onToggleRam256 = { viewModel.toggleRam256Profile() },
                    onToggleCpuBoost = { viewModel.toggleCpuBoost() },
                    onToggleBgOpt = { viewModel.toggleBgOptimization() },
                    onRunManualOptimization = { viewModel.runManualOptimization() },
                    onBrightnessChanged = { viewModel.setBrightness(it) },
                )
            }

            // MIUI Lock Screen overlay
            AnimatedVisibility(
                visible = state.isLocked,
                enter = fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            ) {
                LockScreenOverlay(
                    state = state,
                    onUnlock = { viewModel.unlockDevice() },
                )
            }
        }

        // Global Reading Mode & Brightness visual overlay
        SystemDisplayOverlay(
            brightnessLevel = if (state.preferences.autoBrightness) 1f else state.preferences.brightnessLevel,
            readingMode = state.preferences.readingMode,
        )
    }
}
