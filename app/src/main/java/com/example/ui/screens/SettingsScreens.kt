package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.OsRoute
import com.example.ui.OsUiState
import com.example.ui.theme.DividerLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiEmerald
import com.example.ui.theme.SearchPillGray
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    state: OsUiState,
    onBack: () -> Unit,
    onNavigate: (OsRoute) -> Unit,
    onOpenWifiSettings: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onOpenWirelessSettings: () -> Unit,
    onOpenDataSettings: () -> Unit,
) {
    BackHandler(onBack = onBack)
    var searchQuery by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
        ) {
            // Top bar matching MIUI Global 10.2 Settings in the screenshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .testTag("settings_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = TextPrimary,
                    )
                }
                Text(
                    text = "Настройки",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            // Rounded gray search pill matching screenshot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SearchPillGray)
                    .padding(horizontal = 14.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Поиск настроек",
                            color = TextMuted,
                            fontSize = 14.sp,
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val match: (String) -> Boolean = { label ->
                searchQuery.isBlank() || label.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    if (match("О телефоне MIUI Global 10.2")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.Info,
                            iconBg = Color(0xFFA0A8B3),
                            title = "О телефоне",
                            trailingValue = "MIUI Global 10.2",
                            testTag = "settings_about_phone",
                            onClick = { onNavigate(OsRoute.AboutPhone) },
                        )
                    }
                    if (match("Обновление системных приложений")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.SystemUpdate,
                            iconBg = Color(0xFFFF8F3F),
                            title = "Обновление системных приложений",
                            onClick = { onNavigate(OsRoute.PerformanceSettings) },
                        )
                    }
                    if (match("Состояние безопасности оптимизация 256 МБ")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.Security,
                            iconBg = MiuiEmerald,
                            title = "Состояние безопасности",
                            trailingValue = "${state.telemetry.optimizationScore}%",
                            onClick = { onNavigate(OsRoute.SecurityCenter) },
                        )
                    }
                }

                item {
                    SectionHeader("БЕСПРОВОДНЫЕ СЕТИ")
                }

                item {
                    if (match("SIM-карты и мобильные сети")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.SimCard,
                            iconBg = Color(0xFF4CAF50),
                            title = "SIM-карты и мобильные сети",
                            onClick = onOpenDataSettings,
                        )
                    }
                    if (match("Wi-Fi сеть")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.Wifi,
                            iconBg = MiuiBlue,
                            title = "Wi-Fi",
                            trailingValue = if (state.telemetry.isWifiConnected) "Подключено" else "Откл.",
                            onClick = onOpenWifiSettings,
                        )
                    }
                    if (match("Bluetooth")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.Bluetooth,
                            iconBg = Color(0xFF3F51B5),
                            title = "Bluetooth",
                            trailingValue = if (state.telemetry.isBluetoothEnabled) "Вкл." else "Откл.",
                            onClick = onOpenBluetoothSettings,
                        )
                    }
                    if (match("Точка доступа Wi-Fi")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.WifiTethering,
                            iconBg = Color(0xFF26A69A),
                            title = "Точка доступа Wi-Fi",
                            trailingValue = "Откл.",
                            onClick = onOpenWirelessSettings,
                        )
                    }
                    if (match("Передача данных")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.DataUsage,
                            iconBg = Color(0xFF29B6F6),
                            title = "Передача данных",
                            onClick = onOpenDataSettings,
                        )
                    }
                    if (match("Ещё сети")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.MoreHoriz,
                            iconBg = Color(0xFF78909C),
                            title = "Ещё",
                            onClick = onOpenWirelessSettings,
                        )
                    }
                }

                item {
                    SectionHeader("ЛИЧНЫЕ И СИСТЕМА")
                }

                item {
                    if (match("Экран яркость режим чтения обои")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.WbSunny,
                            iconBg = MiuiAmber,
                            title = "Экран",
                            onClick = { onNavigate(OsRoute.DisplaySettings) },
                        )
                    }
                    if (match("Производительность 256 МБ ОЗУ ЦП 1.5 ГГц")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.Speed,
                            iconBg = MiuiBlue,
                            title = "ОЗУ 256 МБ и ЦП ≥1.5 ГГц",
                            trailingValue = "${String.format("%.2f", state.telemetry.cpuCurrentGhz)} ГГц",
                            testTag = "settings_performance",
                            onClick = { onNavigate(OsRoute.PerformanceSettings) },
                        )
                    }
                    if (match("Рабочий стол и доступ по умолчанию")) {
                        MiuiSettingsRow(
                            icon = Icons.Filled.Home,
                            iconBg = Color(0xFF7E57C2),
                            title = "Рабочий стол и доступ",
                            trailingValue = if (state.isDefaultHome) "По умолчанию" else "Настроить",
                            onClick = { onNavigate(OsRoute.LauncherAccess) },
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AboutPhoneScreen(
    state: OsUiState,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    SettingsSubPageScaffold(title = "О телефоне", onBack = onBack) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceLight,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "MIUI Global 10.2",
                    color = MiuiBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Стабильная оболочка рабочего стола и оптимизации",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SpecDetailTile("Версия ОС / Оболочка", "MIUI Global 10.2 (Android ${android.os.Build.VERSION.RELEASE})")
        SpecDetailTile("Оперативная память ОС", "${state.telemetry.osTargetRamMb} МБ выделено (всего на устройстве: ${state.telemetry.deviceTotalRamMb} МБ)")
        SpecDetailTile("Память фоновой оптимизации", "${state.telemetry.bgOptimizationPoolMb} МБ резерв (${state.telemetry.optimizationBufferAllocatedKb} КБ активно)")
        SpecDetailTile("Процессор (ЦП)", "${state.telemetry.cpuCores} ядер · ${String.format("%.2f", state.telemetry.cpuCurrentGhz)} ГГц (мин. порог 1.50 ГГц)")
        SpecDetailTile("Плавность интерфейса", "${state.telemetry.currentFps} FPS · Архитектура ${state.telemetry.cpuArch}")
        SpecDetailTile("Доступно ОЗУ устройства", "${state.telemetry.deviceAvailableRamMb} МБ свободно")
    }
}

@Composable
fun PerformanceSettingsScreen(
    state: OsUiState,
    onBack: () -> Unit,
    onToggleRam256: () -> Unit,
    onToggleBgOpt: () -> Unit,
    onToggleCpuBoost: () -> Unit,
    onRunOptimization: () -> Unit,
) {
    BackHandler(onBack = onBack)
    SettingsSubPageScaffold(title = "ОЗУ 256 МБ и ЦП ≥1.5 ГГц", onBack = onBack) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MiuiBlue.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Memory, contentDescription = null, tint = MiuiBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Профиль 256 МБ ОЗУ + 1.5+ ГГц",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Выделено ${state.telemetry.osTargetRamMb} МБ для оболочки (${state.telemetry.bgOptimizationPoolMb} МБ для фоновой оптимизации). " +
                        "Текущая частота ЦП: ${String.format("%.2f", state.telemetry.cpuCurrentGhz)} ГГц, плавность: ${state.telemetry.currentFps} FPS.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SettingsSwitchTile(
            title = "Выделение 256 МБ ОЗУ системы",
            subtitle = "Гарантированный объём памяти для стабильной работы рабочего стола",
            checked = state.preferences.ram256Enabled,
            onCheckedChange = { onToggleRam256() },
        )
        SettingsSwitchTile(
            title = "Фоновые процессы оптимизации",
            subtitle = "Собственный резерв ${state.telemetry.bgOptimizationPoolMb} МБ для автоматической очистки кэша",
            checked = state.preferences.bgOptimizationEnabled,
            onCheckedChange = { onToggleBgOpt() },
        )
        SettingsSwitchTile(
            title = "Режим ЦП от 1.5 ГГц (Плавность UI)",
            subtitle = "Приоритет частоты от 1.50 ГГц и аппаратное ускорение анимаций",
            checked = state.preferences.cpu15GhzBoostEnabled,
            onCheckedChange = { onToggleCpuBoost() },
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onRunOptimization,
            enabled = !state.isOptimizing,
            colors = ButtonDefaults.buttonColors(containerColor = MiuiBlue),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                text = if (state.isOptimizing) "Оптимизация памяти..." else "Выполнить оптимизацию памяти сейчас",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun DisplaySettingsScreen(
    state: OsUiState,
    onBack: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onToggleAutoBrightness: () -> Unit,
    onToggleReadingMode: () -> Unit,
    onSetWallpaperUri: (Uri?) -> Unit,
) {
    BackHandler(onBack = onBack)
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onSetWallpaperUri(uri)
    }

    SettingsSubPageScaffold(title = "Экран", onBack = onBack) {
        Text("Уровень яркости", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Slider(
            value = state.preferences.brightnessLevel,
            onValueChange = onBrightnessChange,
            valueRange = 0.15f..1.0f,
            colors = SliderDefaults.colors(thumbColor = MiuiBlue, activeTrackColor = MiuiBlue),
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingsSwitchTile(
            title = "Автояркость",
            subtitle = "Адаптивная регулировка подсветки экрана",
            checked = state.preferences.autoBrightness,
            onCheckedChange = { onToggleAutoBrightness() },
        )
        SettingsSwitchTile(
            title = "Режим чтения",
            subtitle = "Тёплый фильтр для снижения нагрузки на глаза",
            checked = state.preferences.readingMode,
            onCheckedChange = { onToggleReadingMode() },
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Icon(Icons.Filled.Wallpaper, contentDescription = null, tint = MiuiBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выбрать свои обои из галереи", color = TextPrimary)
        }
        if (state.preferences.customWallpaperUri != null) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { onSetWallpaperUri(null) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text("Вернуть стандартные обои MIUI 10", color = MiuiBlue)
            }
        }
    }
}

@Composable
fun LauncherAccessScreen(
    state: OsUiState,
    onBack: () -> Unit,
    onRequestDefaultHome: () -> Unit,
    onOpenHomeSettings: () -> Unit,
) {
    BackHandler(onBack = onBack)
    SettingsSubPageScaffold(title = "Рабочий стол и доступ", onBack = onBack) {
        SpecDetailTile(
            title = "Статус рабочего стола",
            value = if (state.isDefaultHome) "Выбран по умолчанию" else "Работает как приложение-оболочка",
        )
        SpecDetailTile(
            title = "Обнаружено приложений",
            value = "${state.installedApps.size} установленных приложений доступно на 2-м экране",
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRequestDefaultHome,
            colors = ButtonDefaults.buttonColors(containerColor = MiuiBlue),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text("Установить рабочим столом по умолчанию", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onOpenHomeSettings,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("Открыть системные настройки Home", color = TextPrimary)
        }
    }
}

@Composable
private fun SettingsSubPageScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
                }
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            HorizontalDivider(color = DividerLight)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                item {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = DividerLight, thickness = 6.dp)
        Text(
            text = title,
            color = TextMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(start = 18.dp, top = 14.dp, bottom = 6.dp),
        )
    }
}

@Composable
private fun MiuiSettingsRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    trailingValue: String? = null,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    val baseModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = baseModifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconBg),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
        )
        if (!trailingValue.isNullOrBlank()) {
            Text(
                text = trailingValue,
                color = TextSecondary,
                fontSize = 12.5.sp,
                modifier = Modifier.padding(end = 6.dp),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp),
        )
    }
    HorizontalDivider(color = DividerLight, modifier = Modifier.padding(start = 58.dp))
}

@Composable
private fun SpecDetailTile(
    title: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Text(text = title, color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = JetBrainsMonoFontFamily,
        )
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = DividerLight)
    }
}

@Composable
private fun SettingsSwitchTile(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MiuiBlue),
        )
    }
    HorizontalDivider(color = DividerLight)
}
