package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.SystemTelemetry
import com.example.ui.components.MiuiWallpaperBackground
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiEmerald
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SetupOnboardingScreen(
    telemetry: SystemTelemetry,
    isInitializing: Boolean,
    progress: Float,
    isDefaultHome: Boolean,
    onRequestDefaultHome: () -> Unit,
    onGrantAccessAndLaunch: () -> Unit,
) {
    val permissionsToRequest = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        onGrantAccessAndLaunch()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MiuiWallpaperBackground(customWallpaperUri = null, blurredScrim = true)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 14.dp,
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MiuiBlue.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "MIUI GLOBAL 10.2",
                            color = MiuiBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Новый рабочий стол",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "При первом запуске разрешите доступ системе для выделения памяти, оптимизации и запуска рабочего стола.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SystemSpecRow(
                        icon = Icons.Filled.Memory,
                        iconTint = MiuiBlue,
                        title = "Операционная система · ${telemetry.osTargetRamMb} МБ ОЗУ",
                        subtitle = "Выделено ${telemetry.osTargetRamMb} МБ оперативной памяти (устройство: ${telemetry.deviceTotalRamMb} МБ)",
                        badge = "${telemetry.osTargetRamMb} МБ",
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SystemSpecRow(
                        icon = Icons.Filled.Security,
                        iconTint = MiuiEmerald,
                        title = "Память фоновой оптимизации",
                        subtitle = "Собственный резерв ${telemetry.bgOptimizationPoolMb} МБ для фоновых процессов ускорения",
                        badge = "${telemetry.bgOptimizationPoolMb} МБ",
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SystemSpecRow(
                        icon = Icons.Filled.Speed,
                        iconTint = MiuiAmber,
                        title = "Процессор от 1.5 ГГц",
                        subtitle = "Частота ${String.format("%.2f", telemetry.cpuCurrentGhz)} ГГц (${telemetry.cpuCores} ядер) — плавность интерфейса",
                        badge = "${String.format("%.2f", telemetry.cpuCurrentGhz)} ГГц",
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AnimatedVisibility(visible = isInitializing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                color = MiuiBlue,
                                trackColor = SurfaceLight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Инициализация 256 МБ ОЗУ и профиля 1.5+ ГГц...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MiuiBlue,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onRequestDefaultHome,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("set_default_home_button"),
                    ) {
                        Icon(
                            imageVector = if (isDefaultHome) Icons.Filled.CheckCircle else Icons.Filled.Home,
                            contentDescription = null,
                            tint = if (isDefaultHome) MiuiEmerald else MiuiBlue,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDefaultHome) {
                                "Рабочий стол по умолчанию активен"
                            } else {
                                "Сделать рабочим столом по умолчанию"
                            },
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (permissionsToRequest.isNotEmpty()) {
                                permissionLauncher.launch(permissionsToRequest)
                            } else {
                                onGrantAccessAndLaunch()
                            }
                        },
                        enabled = !isInitializing,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MiuiBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("grant_access_button"),
                    ) {
                        Text(
                            text = if (isInitializing) "Запуск рабочего стола..." else "Разрешить доступ и запустить",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemSpecRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLight)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.14f)),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = badge,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = iconTint,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f))
                .padding(horizontal = 7.dp, vertical = 4.dp),
        )
    }
}
