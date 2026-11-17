package com.example.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.system.CallScreenActionType
import com.example.system.InstalledAppInfo
import com.example.system.VirtualAppEntry
import com.example.ui.OsUiState
import com.example.ui.theme.DividerLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiEmerald
import com.example.ui.theme.MiuiRose
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BatteryCalmModeScreen(
    state: OsUiState,
    onBack: () -> Unit,
    onToggleAuto: () -> Unit,
    onSelectThreshold: (Int) -> Unit,
    onToggleManual: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val active = state.isBatteryCalmActive

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = if (active) {
                                listOf(Color(0xFF0D9488), Color(0xFF1BC86A))
                            } else {
                                listOf(Color(0xFF1A8CFF), Color(0xFF0B69D4))
                            },
                        ),
                    )
                    .padding(bottom = 24.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("battery_calm_back")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                        }
                        Text(
                            text = "Режим покоя и батареи 80% / 70%",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Icon(
                            imageVector = if (active) Icons.Filled.NotificationsPaused else Icons.Filled.BatterySaver,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${state.telemetry.batteryPercent}%",
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (active) {
                                "Режим активен — телефон не раздражается и экономит заряд"
                            } else {
                                "Ожидание порога ${state.preferences.batteryCalmThreshold}%"
                            },
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 13.5.sp,
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceWhite,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Порог автоматического включения",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Когда заряд опускается до выбранного порога (80% или 70%), снижается фоновая нагрузка и отключаются отвлекающие сигналы.",
                            color = TextSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(80, 70).forEach { threshold ->
                                val selected = state.preferences.batteryCalmThreshold == threshold
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectThreshold(threshold) },
                                    label = { Text("При $threshold% и ниже") },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MiuiEmerald.copy(alpha = 0.16f),
                                        selectedLabelColor = MiuiEmerald,
                                    ),
                                    modifier = Modifier.testTag("threshold_$threshold"),
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceWhite,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleAuto() }
                                .padding(vertical = 10.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Автовключение по заряду", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "Автоматически активировать при ≤ ${state.preferences.batteryCalmThreshold}%",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                )
                            }
                            Switch(
                                checked = state.preferences.batteryCalmAutoEnabled,
                                onCheckedChange = { onToggleAuto() },
                                colors = SwitchDefaults.colors(checkedTrackColor = MiuiEmerald),
                            )
                        }
                        HorizontalDivider(color = DividerLight)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleManual() }
                                .padding(vertical = 10.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Включить режим вручную", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "Принудительно активировать тихий энергосберегающий режим сейчас",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                )
                            }
                            Switch(
                                checked = state.preferences.batteryCalmManualOverride,
                                onCheckedChange = { onToggleManual() },
                                colors = SwitchDefaults.colors(checkedTrackColor = MiuiEmerald),
                                modifier = Modifier.testTag("battery_calm_manual_switch"),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VirtualSpaceScreen(
    state: OsUiState,
    onBack: () -> Unit,
    onCreateAccount: (String, String) -> Unit,
    onSelectAccount: (String) -> Unit,
    onRegenerateMac: (String?) -> Unit,
    onImportApk: (Uri) -> Unit,
    onCloneApp: (InstalledAppInfo) -> Unit,
    onLaunchVirtualApp: (VirtualAppEntry) -> Unit,
    onLaunchRealPackage: (String) -> Unit,
    onCloseVirtualSession: () -> Unit,
    onRemoveVirtualApp: (String) -> Unit,
) {
    BackHandler(onBack = onBack)

    var showNewAccountDialog by remember { mutableStateOf(false) }
    var showEditMacDialog by remember { mutableStateOf(false) }
    var showCloneAppPicker by remember { mutableStateOf(false) }

    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) onImportApk(uri)
    }

    val activeAccount = state.activeVirtualAccount
    val appsInAccount = remember(state.virtualApps, activeAccount) {
        if (activeAccount == null) emptyList()
        else state.virtualApps.filter { it.accountId == activeAccount.id }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF4F46E5), Color(0xFF0EA5E9)),
                        ),
                    )
                    .padding(bottom = 18.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("virtual_space_back")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                        }
                        Text(
                            text = "Виртуальная среда",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { showNewAccountDialog = true },
                            modifier = Modifier.testTag("create_second_account_btn"),
                        ) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("2-й аккаунт", color = Color.White, fontSize = 12.5.sp)
                        }
                    }

                    // Virtual Account switcher row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                    ) {
                        state.virtualAccounts.forEach { acc ->
                            val isSelected = acc.id == activeAccount?.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { onSelectAccount(acc.id) },
                            ) {
                                Text(
                                    text = acc.displayName,
                                    color = if (isSelected) Color(0xFF4F46E5) else Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                // Active Account + Dedicated MAC Address + Separate 8 GB RAM card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Router, contentDescription = null, tint = MiuiBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeAccount?.displayName ?: "Второй аккаунт",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = activeAccount?.accountHandle ?: "second.user@miui.virtual",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceLight)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Свой MAC-адрес среды", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = activeAccount?.virtualMac ?: "02:00:00:00:00:00",
                                        color = TextPrimary,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.testTag("virtual_mac_text"),
                                    )
                                }
                                IconButton(onClick = { showEditMacDialog = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Редактировать MAC", tint = MiuiBlue)
                                }
                                IconButton(
                                    onClick = { onRegenerateMac(null) },
                                    modifier = Modifier.testTag("regenerate_mac_button"),
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Сгенерировать новый MAC", tint = MiuiEmerald)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MiuiBlue.copy(alpha = 0.08f))
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                            ) {
                                Icon(Icons.Filled.Memory, contentDescription = null, tint = MiuiBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Отдельная ОЗУ: ${state.virtualRam8Gb.virtualRamTotalGb} ГБ (${state.virtualRam8Gb.virtualRamTotalMb} МБ) · Активных страниц: ${state.virtualRam8Gb.committedPagesCount}",
                                    color = MiuiBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }

                // Actions: Add APK or Clone Installed App into Isolated Sandbox
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(
                            onClick = {
                                apkPickerLauncher.launch(arrayOf("application/vnd.android.package-archive", "*/*"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MiuiBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("import_apk_button"),
                        ) {
                            Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Выбрать APK", color = Color.White, fontSize = 13.5.sp)
                        }

                        OutlinedButton(
                            onClick = { showCloneAppPicker = true },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("clone_app_button"),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = MiuiBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Клонировать", color = TextPrimary, fontSize = 13.5.sp)
                        }
                    }
                }

                if (appsInAccount.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SurfaceWhite,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp),
                            ) {
                                Icon(Icons.Filled.Android, contentDescription = null, tint = MiuiBlue, modifier = Modifier.size(38.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Изолированная среда готова",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Выберите .apk файл или клонируйте установленное приложение для запуска во втором аккаунте со своим MAC-адресом.",
                                    color = TextSecondary,
                                    fontSize = 12.5.sp,
                                )
                            }
                        }
                    }
                } else {
                    items(appsInAccount, key = { it.id }) { vApp ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceWhite,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp),
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MiuiBlue.copy(alpha = 0.12f)),
                                ) {
                                    Icon(Icons.Filled.Android, contentDescription = null, tint = MiuiBlue)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(vApp.label, color = TextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${vApp.packageName} · ${vApp.sourceType}",
                                        color = TextSecondary,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "MAC: ${vApp.virtualMac} · Запусков: ${vApp.launchCount}",
                                        color = MiuiEmerald,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.5.sp,
                                    )
                                }
                                IconButton(onClick = { onLaunchVirtualApp(vApp) }) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Запустить", tint = MiuiBlue)
                                }
                                IconButton(onClick = { onRemoveVirtualApp(vApp.id) }) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Удалить", tint = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Active Isolated Virtual Container Session modal
    val activeSession = state.activeVirtualSessionApp
    if (activeSession != null) {
        Dialog(onDismissRequest = onCloseVirtualSession) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceWhite,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = MiuiEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Контейнер: ${activeSession.label}",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Пакет: ${activeSession.packageName}", color = TextSecondary, fontSize = 12.5.sp)
                    Text("Второй аккаунт: ${activeAccount?.displayName ?: "Профиль"}", color = TextSecondary, fontSize = 12.5.sp)
                    Text("Виртуальный MAC: ${activeSession.virtualMac}", color = MiuiBlue, fontFamily = JetBrainsMonoFontFamily, fontSize = 12.5.sp)
                    Text("Выделено ОЗУ: 8 ГБ (виртуальная) + 256 МБ (ОС)", color = MiuiEmerald, fontSize = 12.5.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (activeSession.sourceType == "SYSTEM_CLONE") {
                        Button(
                            onClick = {
                                onCloseVirtualSession()
                                onLaunchRealPackage(activeSession.packageName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MiuiBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Открыть приложение", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedButton(
                        onClick = onCloseVirtualSession,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Закрыть контейнер", color = TextPrimary)
                    }
                }
            }
        }
    }

    if (showNewAccountDialog) {
        var name by remember { mutableStateOf("") }
        var handle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewAccountDialog = false },
            title = { Text("Создать второй аккаунт") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название (напр. Работа)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = handle,
                        onValueChange = { handle = it },
                        label = { Text("Логин / Email аккаунта") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreateAccount(name, handle)
                        showNewAccountDialog = false
                    },
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewAccountDialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }

    if (showEditMacDialog) {
        var macInput by remember { mutableStateOf(activeAccount?.virtualMac ?: "02:AA:BB:CC:DD:EE") }
        AlertDialog(
            onDismissRequest = { showEditMacDialog = false },
            title = { Text("Назначить MAC-адрес") },
            text = {
                OutlinedTextField(
                    value = macInput,
                    onValueChange = { macInput = it },
                    label = { Text("Формат XX:XX:XX:XX:XX:XX") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRegenerateMac(macInput)
                        showEditMacDialog = false
                    },
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditMacDialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }

    if (showCloneAppPicker) {
        Dialog(onDismissRequest = { showCloneAppPicker = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = SurfaceWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Клонировать в среду", fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showCloneAppPicker = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Закрыть")
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.installedApps) { app ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCloneApp(app)
                                        showCloneAppPicker = false
                                    }
                                    .padding(vertical = 8.dp),
                            ) {
                                if (app.icon != null) {
                                    Image(
                                        bitmap = app.icon,
                                        contentDescription = app.label,
                                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)),
                                    )
                                } else {
                                    Icon(Icons.Filled.Apps, contentDescription = null, tint = MiuiBlue)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(app.label, color = TextPrimary, fontSize = 14.sp)
                            }
                            HorizontalDivider(color = DividerLight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallScreeningScreen(
    state: OsUiState,
    onBack: () -> Unit,
    onRequestCallScreeningRole: () -> Unit,
    onToggleFilter: () -> Unit,
    onCheckIncomingNumber: (String) -> Unit,
    onResolveUnknown: (String, Boolean) -> Unit,
    onRemoveRule: (String) -> Unit,
    onContactsPermissionChanged: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val contactsPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
        onContactsPermissionChanged()
    }

    var numberInput by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
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
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0284C7), Color(0xFF1A8CFF)),
                        ),
                    )
                    .padding(bottom = 16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("call_screening_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                    Column {
                        Text(
                            text = "Фильтрация звонков",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Контакты пропускаем · Неизвестные: Разрешить / Отклонить",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 11.5.sp,
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                // Pending Unknown Caller Prompt
                val pending = state.pendingUnknownPrompt
                if (pending != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFFFF7ED),
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.PhoneCallback, contentDescription = null, tint = MiuiAmber)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Неизвестный номер: ${pending.rawNumber}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Номера нет в контактах устройства. Разрешить или отклонить вызов?",
                                    color = TextSecondary,
                                    fontSize = 12.5.sp,
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { onResolveUnknown(pending.normalizedNumber, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MiuiEmerald),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("allow_unknown_call_btn"),
                                    ) {
                                        Text("Разрешить", color = Color.White)
                                    }
                                    Button(
                                        onClick = { onResolveUnknown(pending.normalizedNumber, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MiuiRose),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("reject_unknown_call_btn"),
                                    ) {
                                        Text("Отклонить", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // System role & Contacts permission setup card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceWhite,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Активная фильтрация звонков", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Автопропуск контактов, уведомление на неизвестные", color = TextSecondary, fontSize = 12.sp)
                                }
                                Switch(
                                    checked = state.preferences.callFilterEnabled,
                                    onCheckedChange = { onToggleFilter() },
                                    colors = SwitchDefaults.colors(checkedTrackColor = MiuiBlue),
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { contactsPermLauncher.launch(Manifest.permission.READ_CONTACTS) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("grant_contacts_btn"),
                            ) {
                                Icon(
                                    imageVector = if (state.hasContactsPermission) Icons.Filled.CheckCircle else Icons.Filled.Contacts,
                                    contentDescription = null,
                                    tint = if (state.hasContactsPermission) MiuiEmerald else MiuiBlue,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state.hasContactsPermission) {
                                        "Доступ к контактам разрешён"
                                    } else {
                                        "Разрешить проверку контактов"
                                    },
                                    color = TextPrimary,
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = onRequestCallScreeningRole,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isCallScreeningRoleHeld) MiuiEmerald else MiuiBlue,
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("set_call_screening_role_btn"),
                            ) {
                                Icon(Icons.Filled.VerifiedUser, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state.isCallScreeningRoleHeld) {
                                        "Системная роль CallScreening активна"
                                    } else {
                                        "Назначить системным фильтром звонков"
                                    },
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }

                // Incoming number verification check tool
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceWhite,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Проверка входящего номера", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Проверьте работу фильтра: номер из контактов пропустится сразу, а неизвестный отправит запрос «Разрешить / Отклонить».",
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedTextField(
                                    value = numberInput,
                                    onValueChange = { numberInput = it },
                                    placeholder = { Text("+7 999 123-45-67") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("call_filter_test_input"),
                                )
                                Button(
                                    onClick = { onCheckIncomingNumber(numberInput) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MiuiBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("call_filter_check_btn"),
                                ) {
                                    Icon(Icons.Filled.Call, contentDescription = "Проверить", tint = Color.White)
                                }
                            }
                        }
                    }
                }

                // Rules & History
                if (state.preferences.allowedCallNumbers.isNotEmpty() || state.preferences.blockedCallNumbers.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SurfaceWhite,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Сохранённые решения", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                state.preferences.allowedCallNumbers.forEach { num ->
                                    RuleRow(number = num, allowed = true, onRemove = { onRemoveRule(num) })
                                }
                                state.preferences.blockedCallNumbers.forEach { num ->
                                    RuleRow(number = num, allowed = false, onRemove = { onRemoveRule(num) })
                                }
                            }
                        }
                    }
                }

                if (state.callHistory.isNotEmpty()) {
                    item {
                        Text(
                            text = "Журнал фильтрации",
                            color = TextSecondary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                    items(state.callHistory, key = { it.id }) { rec ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceWhite,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp),
                            ) {
                                Icon(
                                    imageVector = when (rec.status) {
                                        CallScreenActionType.REJECTED_BY_USER -> Icons.Filled.Block
                                        CallScreenActionType.PENDING_UNKNOWN -> Icons.Filled.PhoneCallback
                                        else -> Icons.Filled.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = when (rec.status) {
                                        CallScreenActionType.REJECTED_BY_USER -> MiuiRose
                                        CallScreenActionType.PENDING_UNKNOWN -> MiuiAmber
                                        else -> MiuiEmerald
                                    },
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rec.contactName?.let { "$it (${rec.rawNumber})" } ?: rec.rawNumber,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = when (rec.status) {
                                            CallScreenActionType.ALLOWED_CONTACT -> "Пропущен · из контактов"
                                            CallScreenActionType.ALLOWED_BY_USER -> "Разрешён пользователем"
                                            CallScreenActionType.REJECTED_BY_USER -> "Отклонён"
                                            CallScreenActionType.PENDING_UNKNOWN -> "Ожидает решения (неизвестный)"
                                        },
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleRow(
    number: String,
    allowed: Boolean,
    onRemove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Icon(
            imageVector = if (allowed) Icons.Filled.CheckCircle else Icons.Filled.Block,
            contentDescription = null,
            tint = if (allowed) MiuiEmerald else MiuiRose,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "$number · ${if (allowed) "Разрешён" else "Отклонён"}",
            color = TextPrimary,
            fontSize = 13.5.sp,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = "Удалить правило", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}
