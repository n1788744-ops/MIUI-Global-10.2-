package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.OsRoute
import com.example.ui.OsUiState
import com.example.ui.components.MiuiWallpaperBackground
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TileIconOff
import com.example.ui.theme.TileOffGray

@Composable
fun ControlShadeOverlay(
    state: OsUiState,
    onCloseShade: () -> Unit,
    onNavigate: (OsRoute) -> Unit,
    onToggleMobileData: () -> Unit,
    onOpenWifiSettings: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onToggleMute: () -> Unit,
    onTakeScreenshotNotice: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onToggleAutoBrightness: () -> Unit,
    onToggleAirplaneMode: () -> Unit,
    onLockDevice: () -> Unit,
    onToggleGps: () -> Unit,
    onToggleAutoRotate: () -> Unit,
    onToggleReadingMode: () -> Unit,
    onToggleRam256: () -> Unit,
    onToggleCpuBoost: () -> Unit,
    onToggleBgOpt: () -> Unit,
    onRunManualOptimization: () -> Unit,
    onBrightnessChanged: (Float) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var dragAccumulated by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulated = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        dragAccumulated += dragAmount
                        if (dragAccumulated < -72f) {
                            onCloseShade()
                            dragAccumulated = 0f
                        }
                    },
                )
            },
    ) {
        MiuiWallpaperBackground(
            customWallpaperUri = state.preferences.customWallpaperUri,
            blurredScrim = true,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .widthIn(max = 560.dp)
                .align(Alignment.Center)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            // Top Clock + Date + Gear Icon matching the middle panel of the screenshot
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Column {
                    Text(
                        text = state.clock.timeText,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                    )
                    Text(
                        text = state.clock.shortDateText,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 13.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ОЗУ ${state.telemetry.osTargetRamMb} МБ · ЦП ${String.format("%.2f", state.telemetry.cpuCurrentGhz)} ГГц · ${state.telemetry.currentFps} FPS",
                        color = Color.White.copy(alpha = 0.80f),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                    )
                }

                IconButton(
                    onClick = { onNavigate(OsRoute.Settings) },
                    modifier = Modifier.testTag("shade_settings_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Настройки",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // White Quick Settings Card (3x4 grid + pager dots) matching screenshot
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        if (page == 0) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    ShadeToggleTile(
                                        label = "Данные",
                                        icon = Icons.Filled.SwapVert,
                                        active = state.preferences.mobileDataEnabled || state.telemetry.isMobileConnected,
                                        onClick = onToggleMobileData,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "Wi-Fi",
                                        icon = Icons.Filled.Wifi,
                                        active = state.telemetry.isWifiConnected,
                                        onClick = onOpenWifiSettings,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "Фонарик",
                                        icon = Icons.Filled.FlashlightOn,
                                        active = state.isFlashlightOn,
                                        onClick = onToggleFlashlight,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "Без звука",
                                        icon = Icons.Filled.NotificationsOff,
                                        active = state.preferences.muteEnabled,
                                        onClick = onToggleMute,
                                        modifier = Modifier.weight(1f),
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    ShadeToggleTile(
                                        label = "Скриншот",
                                        icon = Icons.Filled.ContentCut,
                                        active = false,
                                        onClick = onTakeScreenshotNotice,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "Bluetooth",
                                        icon = Icons.Filled.Bluetooth,
                                        active = state.telemetry.isBluetoothEnabled,
                                        onClick = onOpenBluetoothSettings,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "Авто",
                                        icon = Icons.Filled.AutoAwesome,
                                        active = state.preferences.autoBrightness,
                                        onClick = onToggleAutoBrightness,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "В самолёте",
                                        icon = Icons.Filled.AirplanemodeActive,
                                        active = state.preferences.airplaneModeEnabled,
                                        onClick = onToggleAirplaneMode,
                                        modifier = Modifier.weight(1f),
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    ShadeToggleTile(
                                        label = "Блокировка",
                                        icon = Icons.Filled.Lock,
                                        active = false,
                                        onClick = onLockDevice,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "GPS",
                                        icon = Icons.Filled.NearMe,
                                        active = state.preferences.gpsEnabled,
                                        onClick = onToggleGps,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "Поворот",
                                        icon = Icons.Filled.ScreenRotation,
                                        active = state.preferences.autoRotate,
                                        onClick = onToggleAutoRotate,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "Чтение",
                                        icon = Icons.Filled.Visibility,
                                        active = state.preferences.readingMode,
                                        onClick = onToggleReadingMode,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        } else {
                            // Page 2: System RAM 256 MB & CPU >= 1.5 GHz quick controls
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    ShadeToggleTile(
                                        label = "ОЗУ 256 МБ",
                                        icon = Icons.Filled.Memory,
                                        active = state.preferences.ram256Enabled,
                                        onClick = onToggleRam256,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "ЦП ≥1.5 ГГц",
                                        icon = Icons.Filled.Speed,
                                        active = state.preferences.cpu15GhzBoostEnabled,
                                        onClick = onToggleCpuBoost,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "Фон. память",
                                        icon = Icons.Filled.AutoAwesome,
                                        active = state.preferences.bgOptimizationEnabled,
                                        onClick = onToggleBgOpt,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ShadeToggleTile(
                                        label = "Ускорить",
                                        icon = Icons.Filled.CleaningServices,
                                        active = state.isOptimizing,
                                        onClick = onRunManualOptimization,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                Spacer(modifier = Modifier.height(148.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Two page indicator dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 0..1) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == i) MiuiBlue else Color(0xFFD0D5DD),
                                    ),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Brightness Slider Pill Card matching screenshot
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.BrightnessLow,
                        contentDescription = "Меньше яркость",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Slider(
                        value = state.preferences.brightnessLevel,
                        onValueChange = onBrightnessChanged,
                        valueRange = 0.15f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = MiuiBlue,
                            activeTrackColor = MiuiBlue,
                            inactiveTrackColor = TileOffGray,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .testTag("shade_brightness_slider"),
                    )
                    Icon(
                        imageVector = Icons.Filled.BrightnessHigh,
                        contentDescription = "Больше яркость",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom drag-up close pill
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCloseShade)
                    .padding(vertical = 12.dp)
                    .testTag("close_shade_handle"),
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.75f)),
                )
            }
        }
    }
}

@Composable
private fun ShadeToggleTile(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (active) MiuiBlue else TileOffGray),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) Color.White else TileIconOff,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
