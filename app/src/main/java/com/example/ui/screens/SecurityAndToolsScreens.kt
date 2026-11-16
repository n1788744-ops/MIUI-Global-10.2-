package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.OsUiState
import com.example.ui.components.MiuiWallpaperBackground
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MiuiAmber
import com.example.ui.theme.MiuiBlue
import com.example.ui.theme.MiuiEmerald
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun SecurityCenterScreen(
    state: OsUiState,
    onBack: () -> Unit,
    onRunOptimization: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val animatedScore by animateFloatAsState(
        targetValue = state.telemetry.optimizationScore.toFloat(),
        animationSpec = tween(durationMillis = 700),
        label = "score",
    )

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
            // Gradient MIUI Security Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0FB87A), Color(0xFF1BC86A)),
                        ),
                    )
                    .padding(bottom = 26.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("security_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                        }
                        Text(
                            text = "Безопасность и оптимизация",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(164.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            drawArc(
                                color = Color.White.copy(alpha = 0.25f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = stroke,
                            )
                            drawArc(
                                color = Color.White,
                                startAngle = 135f,
                                sweepAngle = (animatedScore / 100f) * 270f,
                                useCenter = false,
                                style = stroke,
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${animatedScore.roundToInt()}",
                                color = Color.White,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Баллы системы",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onRunOptimization,
                        enabled = !state.isOptimizing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("security_optimize_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = MiuiEmerald,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isOptimizing) "Оптимизация..." else "Оптимизировать память",
                            color = MiuiEmerald,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                TelemetryMetricCard(
                    icon = Icons.Filled.Memory,
                    accent = MiuiBlue,
                    title = "ОЗУ операционной системы: ${state.telemetry.osTargetRamMb} МБ",
                    detail = "Собственный пул приложения: ${state.telemetry.appActivePoolMb} МБ активной оболочки + " +
                        "${state.telemetry.bgOptimizationPoolMb} МБ для фоновых процессов оптимизации.",
                    monoBadge = "Heap: ${state.telemetry.jvmUsedHeapMb}/${state.telemetry.jvmMaxHeapMb} МБ · Устр: ${state.telemetry.deviceAvailableRamMb} МБ своб.",
                )

                TelemetryMetricCard(
                    icon = Icons.Filled.Shield,
                    accent = MiuiEmerald,
                    title = "Фоновая оптимизация памяти",
                    detail = "Активный буфер оптимизации: ${state.telemetry.optimizationBufferAllocatedKb} КБ · " +
                        "Циклов очистки: ${state.telemetry.optimizationCyclesRun} · Освобождено: ${state.telemetry.lastOptimizationFreedMb} МБ.",
                    monoBadge = if (state.preferences.bgOptimizationEnabled) "АВТО-ОПТИМИЗАЦИЯ ВКЛ" else "ПАУЗА",
                )

                TelemetryMetricCard(
                    icon = Icons.Filled.Speed,
                    accent = MiuiAmber,
                    title = "Процессор ≥ 1.5 ГГц (Плавность)",
                    detail = "Текущая частота: ${String.format("%.2f", state.telemetry.cpuCurrentGhz)} ГГц " +
                        "(Макс: ${String.format("%.2f", state.telemetry.cpuMaxGhz)} ГГц, ядер: ${state.telemetry.cpuCores}).",
                    monoBadge = "${state.telemetry.currentFps} FPS · ПЛАВНЫЙ ИНТЕРФЕЙС",
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetricCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    detail: String,
    monoBadge: String,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(detail, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = monoBadge,
                color = accent,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var display by remember { mutableStateOf("0") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var resetOnNextDigit by remember { mutableStateOf(false) }

    fun pressDigit(d: String) {
        display = if (display == "0" || resetOnNextDigit) {
            resetOnNextDigit = false
            d
        } else {
            (display + d).take(14)
        }
    }

    fun evaluate() {
        val a = firstOperand ?: return
        val b = display.toDoubleOrNull() ?: return
        val result = when (pendingOp) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (b != 0.0) a / b else Double.NaN
            else -> b
        }
        display = if (result.isNaN()) "Ошибка"
        else if (result % 1.0 == 0.0) result.toLong().toString()
        else String.format("%.4f", result).trimEnd('0').trimEnd('.')
        firstOperand = null
        pendingOp = null
        resetOnNextDigit = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
            }
            Text("Калькулятор", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
        ) {
            Text(
                text = display,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = TextPrimary,
                maxLines = 1,
            )
        }

        val rows = listOf(
            listOf("C", "÷", "×", "⌫"),
            listOf("7", "8", "9", "-"),
            listOf("4", "5", "6", "+"),
            listOf("1", "2", "3", "="),
            listOf("0", ".", "%", "="),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { key ->
                        val isOp = key in listOf("+", "-", "×", "÷", "=", "C", "⌫", "%")
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (key == "=") MiuiAmber else if (isOp) SurfaceLight else Color(0xFFF8F9FB))
                                .clickable {
                                    when (key) {
                                        "C" -> {
                                            display = "0"
                                            firstOperand = null
                                            pendingOp = null
                                        }
                                        "⌫" -> display = if (display.length > 1) display.dropLast(1) else "0"
                                        "+", "-", "×", "÷" -> {
                                            firstOperand = display.toDoubleOrNull()
                                            pendingOp = key
                                            resetOnNextDigit = true
                                        }
                                        "%" -> {
                                            val v = display.toDoubleOrNull() ?: 0.0
                                            display = (v / 100.0).toString()
                                        }
                                        "=" -> evaluate()
                                        "." -> if (!display.contains(".")) display += "."
                                        else -> pressDigit(key)
                                    }
                                },
                        ) {
                            Text(
                                text = key,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (key == "=") Color.White else if (isOp) MiuiAmber else TextPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompassScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var azimuthDeg by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accel = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnet = sm?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (sm == null || accel == null || magnet == null) {
            sensorAvailable = false
            onDispose { }
        } else {
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            val rMat = FloatArray(9)
            val iMat = FloatArray(9)
            val orientation = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                    }
                    if (SensorManager.getRotationMatrix(rMat, iMat, gravity, geomagnetic)) {
                        SensorManager.getOrientation(rMat, orientation)
                        val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        azimuthDeg = (deg + 360f) % 360f
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sm.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(listener, magnet, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(listener) }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF11161D))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Text("Компас", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension / 2f
                drawCircle(color = Color(0xFF1E2631), radius = r)
                drawCircle(color = MiuiBlue, radius = r, style = Stroke(width = 4.dp.toPx()))
                val rad = Math.toRadians((-azimuthDeg - 90f).toDouble())
                val end = Offset(
                    x = center.x + (r * 0.72f * kotlin.math.cos(rad)).toFloat(),
                    y = center.y + (r * 0.72f * kotlin.math.sin(rad)).toFloat(),
                )
                drawLine(color = Color(0xFFEF4444), start = center, end = end, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
            }
            Text(
                text = "${azimuthDeg.roundToInt()}°",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Light,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (sensorAvailable) "Датчик магнитного поля активен" else "Магнитный датчик не обнаружен на устройстве",
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun LockScreenOverlay(
    state: OsUiState,
    onUnlock: () -> Unit,
) {
    BackHandler(onBack = onUnlock)
    var dragSum by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragSum = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        dragSum += dragAmount
                        if (dragSum < -80f) {
                            onUnlock()
                            dragSum = 0f
                        }
                    },
                )
            },
    ) {
        MiuiWallpaperBackground(customWallpaperUri = state.preferences.customWallpaperUri, blurredScrim = false)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f)),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 48.dp, horizontal = 24.dp),
        ) {
            Text(
                text = state.clock.timeText,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 68.sp),
                color = Color.White,
            )
            Text(
                text = state.clock.fullDateText,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onUnlock)
                    .padding(16.dp)
                    .testTag("lock_screen_unlock"),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Разблокировать",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "Проведите вверх для разблокировки",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                )
            }
        }
    }
}
