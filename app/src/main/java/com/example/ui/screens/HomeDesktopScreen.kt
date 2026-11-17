package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.system.InstalledAppInfo
import com.example.ui.DesktopFolder
import com.example.ui.OsRoute
import com.example.ui.OsUiState
import com.example.ui.components.GoogleMulticolorBadge
import com.example.ui.components.MiuiDesktopAppItem
import com.example.ui.components.MiuiIconKind
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeDesktopScreen(
    state: OsUiState,
    onOpenShade: () -> Unit,
    onNavigate: (OsRoute) -> Unit,
    onOpenFolder: (DesktopFolder?) -> Unit,
    onSetSearchOpen: (Boolean) -> Unit,
    onLaunchInstalledApp: (InstalledAppInfo) -> Unit,
    onLaunchBrowser: () -> Unit,
    onLaunchMusic: () -> Unit,
    onLaunchPlayStore: () -> Unit,
    onLaunchDialer: () -> Unit,
    onLaunchMessages: () -> Unit,
    onLaunchCamera: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onPickWallpaper: (Uri?) -> Unit,
    onResolveUnknownCall: (String, Boolean) -> Unit,
    onShowToast: (String) -> Unit,
) {
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            onPickWallpaper(uri)
            onShowToast("Изображение применено как обои рабочего стола")
        }
    }

    val appChunks = remember(state.installedApps) {
        if (state.installedApps.isEmpty()) emptyList() else state.installedApps.chunked(12)
    }
    val totalPages = 1 + appChunks.size
    val pagerState = rememberPagerState(pageCount = { totalPages })

    var dragAccumulated by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulated = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        dragAccumulated += dragAmount
                        if (dragAccumulated > 72f) {
                            onOpenShade()
                            dragAccumulated = 0f
                        }
                    },
                )
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .widthIn(max = 560.dp)
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
        ) {
            // Compact MIUI status & quick-shade handle bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenShade)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .testTag("open_control_shade_bar"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val ramBadge = if (state.preferences.virtualRam8GbEnabled) {
                        "8 ГБ + ${state.telemetry.osTargetRamMb} МБ"
                    } else {
                        "${state.telemetry.osTargetRamMb} МБ"
                    }
                    Text(
                        text = "$ramBadge · ${String.format("%.1f", state.telemetry.cpuCurrentGhz)} ГГц",
                        color = Color.White.copy(alpha = 0.92f),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    if (state.isBatteryCalmActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Покой ${state.preferences.batteryCalmThreshold}%",
                            color = Color(0xFFA7F3D0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Открыть шторку управления",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.telemetry.isWifiConnected) {
                        Icon(
                            imageVector = Icons.Filled.Wifi,
                            contentDescription = "Wi-Fi подключен",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        imageVector = if (state.telemetry.isCharging) {
                            Icons.Filled.BatteryChargingFull
                        } else {
                            Icons.Filled.BatteryFull
                        },
                        contentDescription = "Батарея",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${state.telemetry.batteryPercent}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Google-style pill search bar matching the screenshot
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("desktop_search_bar")
                    .clickable { onSetSearchOpen(true) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    GoogleMulticolorBadge(size = 20.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Поиск приложений и настроек",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Голосовой поиск",
                        tint = MiuiBlue,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Live unknown-caller Allow/Reject prompt banner when CallScreening catches an unknown caller
            val pendingCall = state.pendingUnknownPrompt
            if (pendingCall != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.96f),
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Filled.PhoneCallback, contentDescription = null, tint = MiuiAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Неизвестный: ${pendingCall.rawNumber}",
                                color = TextPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "Разрешить или отклонить?",
                                color = TextSecondary,
                                fontSize = 11.sp,
                            )
                        }
                        Text(
                            text = "Разрешить",
                            color = MiuiEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onResolveUnknownCall(pendingCall.normalizedNumber, true) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                        )
                        Text(
                            text = "Отклонить",
                            color = Color(0xFFE93E6C),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onResolveUnknownCall(pendingCall.normalizedNumber, false) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Large thin clock + date on the left, weather/system pill on the right (1:1 with screenshot)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
            ) {
                Column {
                    Text(
                        text = state.clock.timeText,
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                    )
                    Text(
                        text = state.clock.fullDateText,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFFFCA28))
                        .clickable { onNavigate(OsRoute.SecurityCenter) }
                        .testTag("weather_sun_badge"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Состояние системы и оптимизация",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Swipeable Desktop Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(216.dp),
            ) { page ->
                if (page == 0) {
                    // Page 1: Exact 2x4 icon grid matching the MIUI Global 10.2 screenshot
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            MiuiDesktopAppItem(
                                label = "Браузер",
                                kind = MiuiIconKind.BROWSER,
                                testTag = "icon_browser",
                                onClick = onLaunchBrowser,
                                modifier = Modifier.weight(1f),
                            )
                            MiuiDesktopAppItem(
                                label = "Галерея",
                                kind = MiuiIconKind.GALLERY,
                                testTag = "icon_gallery",
                                onClick = {
                                    galleryPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                            MiuiDesktopAppItem(
                                label = "Настройки",
                                kind = MiuiIconKind.SETTINGS,
                                testTag = "icon_settings",
                                onClick = { onNavigate(OsRoute.Settings) },
                                modifier = Modifier.weight(1f),
                            )
                            MiuiDesktopAppItem(
                                label = "Музыка",
                                kind = MiuiIconKind.MUSIC,
                                testTag = "icon_music",
                                onClick = onLaunchMusic,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            MiuiDesktopAppItem(
                                label = "Google",
                                kind = MiuiIconKind.GOOGLE_FOLDER,
                                testTag = "icon_google_folder",
                                onClick = { onOpenFolder(DesktopFolder.GOOGLE) },
                                modifier = Modifier.weight(1f),
                            )
                            MiuiDesktopAppItem(
                                label = "Безопасность",
                                kind = MiuiIconKind.SECURITY,
                                testTag = "icon_security",
                                onClick = { onNavigate(OsRoute.SecurityCenter) },
                                modifier = Modifier.weight(1f),
                            )
                            MiuiDesktopAppItem(
                                label = "Инструменты",
                                kind = MiuiIconKind.TOOLS_FOLDER,
                                testTag = "icon_tools_folder",
                                onClick = { onOpenFolder(DesktopFolder.TOOLS) },
                                modifier = Modifier.weight(1f),
                            )
                            MiuiDesktopAppItem(
                                label = "Play Маркет",
                                kind = MiuiIconKind.PLAY_STORE,
                                badgeCount = 1,
                                testTag = "icon_play_store",
                                onClick = onLaunchPlayStore,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                } else {
                    // Page 2+: Real installed Android applications on the device
                    val appsOnPage = appChunks.getOrElse(page - 1) { emptyList() }
                    val rows = appsOnPage.chunked(4)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        rows.forEach { rowApps ->
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                rowApps.forEach { app ->
                                    InstalledAppGridItem(
                                        app = app,
                                        onClick = { onLaunchInstalledApp(app) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                repeat(4 - rowApps.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val dotCount = totalPages.coerceAtLeast(2)
                for (index in 0 until dotCount) {
                    val active = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (active) 7.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) Color.White else Color.White.copy(alpha = 0.45f),
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom 4-icon Dock matching the screenshot (Phone, Messages, Chrome, Camera)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
            ) {
                MiuiDesktopAppItem(
                    label = null,
                    kind = MiuiIconKind.PHONE,
                    testTag = "dock_phone",
                    iconSize = 54.dp,
                    onClick = onLaunchDialer,
                    modifier = Modifier.weight(1f),
                )
                MiuiDesktopAppItem(
                    label = null,
                    kind = MiuiIconKind.MESSAGES,
                    testTag = "dock_messages",
                    iconSize = 54.dp,
                    onClick = onLaunchMessages,
                    modifier = Modifier.weight(1f),
                )
                MiuiDesktopAppItem(
                    label = null,
                    kind = MiuiIconKind.CHROME,
                    testTag = "dock_chrome",
                    iconSize = 54.dp,
                    onClick = onLaunchBrowser,
                    modifier = Modifier.weight(1f),
                )
                MiuiDesktopAppItem(
                    label = null,
                    kind = MiuiIconKind.CAMERA,
                    testTag = "dock_camera",
                    iconSize = 54.dp,
                    onClick = onLaunchCamera,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Folder popup modal (Google or Инструменты)
        val activeFolder = state.activeFolder
        if (activeFolder != null) {
            FolderOverlayDialog(
                folder = activeFolder,
                isFlashlightOn = state.isFlashlightOn,
                onDismiss = { onOpenFolder(null) },
                onLaunchBrowser = onLaunchBrowser,
                onLaunchPlayStore = onLaunchPlayStore,
                onNavigate = onNavigate,
                onToggleFlashlight = onToggleFlashlight,
                onOpenSettings = { onNavigate(OsRoute.Settings) },
                onShowToast = onShowToast,
            )
        }

        // Search Sheet modal
        if (state.isSearchOpen) {
            DesktopSearchDialog(
                installedApps = state.installedApps,
                onDismiss = { onSetSearchOpen(false) },
                onLaunchInstalledApp = onLaunchInstalledApp,
                onNavigate = onNavigate,
                onLaunchBrowser = onLaunchBrowser,
            )
        }
    }
}

@Composable
private fun InstalledAppGridItem(
    app: InstalledAppInfo,
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
        if (app.icon != null) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp)),
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MiuiBlue),
            ) {
                Icon(
                    imageVector = Icons.Filled.Apps,
                    contentDescription = app.label,
                    tint = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FolderOverlayDialog(
    folder: DesktopFolder,
    isFlashlightOn: Boolean,
    onDismiss: () -> Unit,
    onLaunchBrowser: () -> Unit,
    onLaunchPlayStore: () -> Unit,
    onNavigate: (OsRoute) -> Unit,
    onToggleFlashlight: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowToast: (String) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.94f),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = folder.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(18.dp))

                if (folder == DesktopFolder.GOOGLE) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FolderMiniAction("Поиск", Icons.Filled.Search, Color(0xFF4285F4)) {
                            onDismiss(); onLaunchBrowser()
                        }
                        FolderMiniAction("Карты", Icons.Filled.Map, Color(0xFF34A853)) {
                            onDismiss(); onLaunchBrowser()
                        }
                        FolderMiniAction("Почта", Icons.Filled.Mail, Color(0xFFEA4335)) {
                            onDismiss(); onShowToast("Открытие почтового клиента")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FolderMiniAction("YouTube", Icons.Filled.PlayArrow, Color(0xFFFF0000)) {
                            onDismiss(); onLaunchBrowser()
                        }
                        FolderMiniAction("Фото", Icons.Filled.PhotoLibrary, Color(0xFFFBBC05)) {
                            onDismiss(); onNavigate(OsRoute.DisplaySettings)
                        }
                        FolderMiniAction("Play", Icons.Filled.Apps, Color(0xFF00ACC1)) {
                            onDismiss(); onLaunchPlayStore()
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FolderMiniAction("Вирт. среда", Icons.Filled.Android, Color(0xFF4F46E5)) {
                            onDismiss(); onNavigate(OsRoute.VirtualSpace)
                        }
                        FolderMiniAction("Фильтр звонков", Icons.Filled.PhoneCallback, MiuiBlue) {
                            onDismiss(); onNavigate(OsRoute.CallScreening)
                        }
                        FolderMiniAction("Режим 80/70%", Icons.Filled.BatterySaver, MiuiEmerald) {
                            onDismiss(); onNavigate(OsRoute.BatteryCalmMode)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FolderMiniAction("Калькулятор", Icons.Filled.Calculate, Color(0xFFF57C00)) {
                            onDismiss(); onNavigate(OsRoute.Calculator)
                        }
                        FolderMiniAction(
                            if (isFlashlightOn) "Фонарь (Вкл)" else "Фонарик",
                            Icons.Filled.FlashlightOn,
                            if (isFlashlightOn) MiuiAmber else Color(0xFF5A626D),
                        ) {
                            onToggleFlashlight()
                        }
                        FolderMiniAction("Компас", Icons.Filled.Explore, MiuiBlue) {
                            onDismiss(); onNavigate(OsRoute.Compass)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FolderMiniAction("ОЗУ 8 ГБ", Icons.Filled.Memory, MiuiEmerald) {
                            onDismiss(); onNavigate(OsRoute.PerformanceSettings)
                        }
                        FolderMiniAction("ЦП 1.5 ГГц", Icons.Filled.Speed, MiuiBlue) {
                            onDismiss(); onNavigate(OsRoute.SecurityCenter)
                        }
                        FolderMiniAction("Настройки", Icons.Filled.Settings, Color(0xFF687584)) {
                            onDismiss(); onOpenSettings()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderMiniAction(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(82.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.14f)),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DesktopSearchDialog(
    installedApps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onLaunchInstalledApp: (InstalledAppInfo) -> Unit,
    onNavigate: (OsRoute) -> Unit,
    onLaunchBrowser: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val systemItems = remember {
        listOf(
            "Настройки MIUI" to OsRoute.Settings,
            "Виртуальная среда (APK, 2-й аккаунт, MAC)" to OsRoute.VirtualSpace,
            "Фильтрация звонков (неизвестные / контакты)" to OsRoute.CallScreening,
            "Режим батареи 80% / 70% (спокойный режим)" to OsRoute.BatteryCalmMode,
            "Оперативная память 8 ГБ + 256 МБ" to OsRoute.PerformanceSettings,
            "Безопасность и оптимизация ОЗУ" to OsRoute.SecurityCenter,
            "О телефоне (MIUI Global 10.2)" to OsRoute.AboutPhone,
            "Экран и яркость" to OsRoute.DisplaySettings,
            "Калькулятор" to OsRoute.Calculator,
        )
    }
    val filteredSystem = remember(query) {
        if (query.isBlank()) systemItems
        else systemItems.filter { it.first.contains(query, ignoreCase = true) }
    }
    val filteredApps = remember(query, installedApps) {
        if (query.isBlank()) installedApps.take(8)
        else installedApps.filter { it.label.contains(query, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Поиск приложений и настроек", fontSize = 14.sp) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MiuiBlue,
                            unfocusedBorderColor = Color(0xFFE0E4EA),
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Закрыть поиск")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredSystem) { (name, route) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    onNavigate(route)
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = MiuiBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(name, color = TextPrimary, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = Color(0xFFF1F3F5))
                    }
                    items(filteredApps) { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLaunchInstalledApp(app) }
                                .padding(vertical = 9.dp, horizontal = 6.dp),
                        ) {
                            if (app.icon != null) {
                                Image(
                                    bitmap = app.icon,
                                    contentDescription = app.label,
                                    modifier = Modifier.size(26.dp).clip(RoundedCornerShape(6.dp)),
                                )
                            } else {
                                Icon(Icons.Filled.Apps, contentDescription = null, tint = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(app.label, color = TextPrimary, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = Color(0xFFF1F3F5))
                    }
                    if (query.isNotBlank()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        onLaunchBrowser()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                            ) {
                                Icon(Icons.Filled.Explore, contentDescription = null, tint = MiuiBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Искать \"$query\" в браузере", color = MiuiBlue, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
