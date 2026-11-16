package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class MiuiIconKind {
    BROWSER,
    GALLERY,
    SETTINGS,
    MUSIC,
    GOOGLE_FOLDER,
    SECURITY,
    TOOLS_FOLDER,
    PLAY_STORE,
    PHONE,
    MESSAGES,
    CHROME,
    CAMERA,
}

@Composable
fun MiuiDesktopAppItem(
    label: String?,
    kind: MiuiIconKind,
    badgeCount: Int? = null,
    testTag: String,
    iconSize: Dp = 54.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick,
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            MiuiIconGraphic(kind = kind, size = iconSize)
            if (badgeCount != null && badgeCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x = 3.dp, y = (-3).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34C759)),
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        if (!label.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun MiuiIconGraphic(
    kind: MiuiIconKind,
    size: Dp = 54.dp,
) {
    val squircle = RoundedCornerShape(15.dp)
    when (kind) {
        MiuiIconKind.BROWSER -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF4FC3F7), Color(0xFF1A8CFF)),
                        ),
                    ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = "Браузер",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f),
                )
            }
        }

        MiuiIconKind.GALLERY -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, squircle)
                    .clip(squircle)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF4DD0E1), Color(0xFF00ACC1)),
                        ),
                    ),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(size * 0.66f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Landscape,
                        contentDescription = "Галерея",
                        tint = Color(0xFF0097A7),
                        modifier = Modifier.size(size * 0.44f),
                    )
                }
            }
        }

        MiuiIconKind.SETTINGS -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, squircle)
                    .clip(squircle)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE5EAF0), Color(0xFFC5CFD9)),
                        ),
                    ),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(size * 0.66f)
                        .clip(CircleShape)
                        .background(Color(0xFF687584)),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Настройки",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.46f),
                    )
                }
            }
        }

        MiuiIconKind.MUSIC -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF5E8E), Color(0xFFD81B60)),
                        ),
                    ),
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = "Музыка",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.56f),
                )
            }
        }

        MiuiIconKind.GOOGLE_FOLDER -> {
            FolderIconSquircle(
                size = size,
                background = Color(0x70FFFFFF),
                dotColors = listOf(
                    Color(0xFF4285F4), Color(0xFFEA4335), Color(0xFFFBBC05),
                    Color(0xFF34A853), Color(0xFF4285F4), Color(0xFFEA4335),
                    Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFF4285F4),
                ),
            )
        }

        MiuiIconKind.SECURITY -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, squircle)
                    .clip(squircle)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2EE58B), Color(0xFF14B864)),
                        ),
                    ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Безопасность",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.64f),
                )
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Color(0xFF14B864),
                    modifier = Modifier.size(size * 0.34f),
                )
            }
        }

        MiuiIconKind.TOOLS_FOLDER -> {
            FolderIconSquircle(
                size = size,
                background = Color(0xFFF57C20),
                dotColors = listOf(
                    Color.White, Color(0xFFFFE082), Color.White,
                    Color(0xFFFFCCBC), Color.White, Color(0xFFFFE082),
                    Color.White, Color(0xFFFFCCBC), Color.White,
                ),
            )
        }

        MiuiIconKind.PLAY_STORE -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, squircle)
                    .clip(squircle)
                    .background(Color.White),
            ) {
                Canvas(modifier = Modifier.size(size * 0.56f)) {
                    val w = this.size.width
                    val h = this.size.height
                    val left = Path().apply {
                        moveTo(w * 0.14f, h * 0.08f)
                        lineTo(w * 0.58f, h * 0.50f)
                        lineTo(w * 0.14f, h * 0.92f)
                        close()
                    }
                    drawPath(left, color = Color(0xFF00C4FF))

                    val top = Path().apply {
                        moveTo(w * 0.14f, h * 0.08f)
                        lineTo(w * 0.92f, h * 0.50f)
                        lineTo(w * 0.58f, h * 0.50f)
                        close()
                    }
                    drawPath(top, color = Color(0xFF34A853))

                    val bottom = Path().apply {
                        moveTo(w * 0.14f, h * 0.92f)
                        lineTo(w * 0.58f, h * 0.50f)
                        lineTo(w * 0.92f, h * 0.50f)
                        close()
                    }
                    drawPath(bottom, color = Color(0xFFEA4335))

                    val tip = Path().apply {
                        moveTo(w * 0.58f, h * 0.50f)
                        lineTo(w * 0.76f, h * 0.40f)
                        lineTo(w * 0.92f, h * 0.50f)
                        lineTo(w * 0.76f, h * 0.60f)
                        close()
                    }
                    drawPath(tip, color = Color(0xFFFBBC05))
                }
            }
        }

        MiuiIconKind.PHONE -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, squircle)
                    .clip(squircle)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF38E27D), Color(0xFF18B858)),
                        ),
                    ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Телефон",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.54f),
                )
            }
        }

        MiuiIconKind.MESSAGES -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, squircle)
                    .clip(squircle)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFB74D), Color(0xFFF57C00)),
                        ),
                    ),
            ) {
                Icon(
                    imageVector = Icons.Filled.ChatBubble,
                    contentDescription = "Сообщения",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.50f),
                )
            }
        }

        MiuiIconKind.CHROME -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
            ) {
                Canvas(modifier = Modifier.size(size * 0.90f)) {
                    val r = this.size.minDimension / 2f
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    drawArc(
                        color = Color(0xFFEA4335),
                        startAngle = 210f,
                        sweepAngle = 150f,
                        useCenter = true,
                    )
                    drawArc(
                        color = Color(0xFFFBBC05),
                        startAngle = 0f,
                        sweepAngle = 120f,
                        useCenter = true,
                    )
                    drawArc(
                        color = Color(0xFF34A853),
                        startAngle = 120f,
                        sweepAngle = 90f,
                        useCenter = true,
                    )
                    drawCircle(
                        color = Color.White,
                        radius = r * 0.44f,
                        center = center,
                    )
                    drawCircle(
                        color = Color(0xFF4285F4),
                        radius = r * 0.34f,
                        center = center,
                    )
                }
            }
        }

        MiuiIconKind.CAMERA -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .shadow(3.dp, squircle)
                    .clip(squircle)
                    .background(Color(0xFFF5F7FA)),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(size * 0.68f)
                        .clip(CircleShape)
                        .background(Color(0xFF2B3038)),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Камера",
                        tint = Color(0xFF90A4AE),
                        modifier = Modifier.size(size * 0.36f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderIconSquircle(
    size: Dp,
    background: Color,
    dotColors: List<Color>,
) {
    val squircle = RoundedCornerShape(15.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .shadow(2.dp, squircle)
            .clip(squircle)
            .background(background)
            .padding(7.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (row in 0..2) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.5.dp)) {
                    for (col in 0..2) {
                        val color = dotColors[row * 3 + col]
                        Box(
                            modifier = Modifier
                                .size((size - 24.dp) / 3f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleMulticolorBadge(size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val stroke = Stroke(width = this.size.minDimension * 0.22f, cap = StrokeCap.Butt)
        drawArc(Color(0xFFEA4335), startAngle = 215f, sweepAngle = 115f, useCenter = false, style = stroke)
        drawArc(Color(0xFFFBBC05), startAngle = 140f, sweepAngle = 75f, useCenter = false, style = stroke)
        drawArc(Color(0xFF34A853), startAngle = 45f, sweepAngle = 95f, useCenter = false, style = stroke)
        drawArc(Color(0xFF4285F4), startAngle = -15f, sweepAngle = 60f, useCenter = false, style = stroke)
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(this.size.width * 0.52f, this.size.height * 0.5f),
            end = Offset(this.size.width * 0.92f, this.size.height * 0.5f),
            strokeWidth = stroke.width,
        )
    }
}
