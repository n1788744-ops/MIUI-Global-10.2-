package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ui.theme.ReadingTint
import com.example.ui.theme.WallpaperBlueBright
import com.example.ui.theme.WallpaperBlueDeep
import com.example.ui.theme.WallpaperGreen
import com.example.ui.theme.WallpaperOrange
import com.example.ui.theme.WallpaperTeal
import com.example.ui.theme.WallpaperYellow

@Composable
fun MiuiWallpaperBackground(
    customWallpaperUri: String?,
    blurredScrim: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (!customWallpaperUri.isNullOrBlank()) {
            AsyncImage(
                model = customWallpaperUri,
                contentDescription = "Обои рабочего стола",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Base vibrant vertical-diagonal gradient matching the MIUI 10 brushstroke wallpaper
                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to WallpaperBlueDeep,
                            0.24f to WallpaperBlueBright,
                            0.45f to WallpaperTeal,
                            0.64f to WallpaperGreen,
                            0.82f to WallpaperYellow,
                            1.00f to WallpaperOrange,
                        ),
                        start = Offset(w * 0.15f, 0f),
                        end = Offset(w * 0.85f, h),
                    ),
                )

                // Sweeping top-right sky-cyan brushstroke curve
                val cyanWave = Path().apply {
                    moveTo(0f, h * 0.10f)
                    cubicTo(
                        w * 0.35f, h * 0.02f,
                        w * 0.65f, h * 0.24f,
                        w, h * 0.14f,
                    )
                    lineTo(w, h * 0.42f)
                    cubicTo(
                        w * 0.68f, h * 0.48f,
                        w * 0.30f, h * 0.26f,
                        0f, h * 0.35f,
                    )
                    close()
                }
                drawPath(
                    path = cyanWave,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x8838BDF8), Color(0xAA00D2B8)),
                        start = Offset(0f, h * 0.1f),
                        end = Offset(w, h * 0.38f),
                    ),
                )

                // Diagonal emerald-jade ribbon through the middle
                val emeraldSweep = Path().apply {
                    moveTo(0f, h * 0.34f)
                    cubicTo(
                        w * 0.38f, h * 0.28f,
                        w * 0.66f, h * 0.52f,
                        w, h * 0.40f,
                    )
                    lineTo(w, h * 0.68f)
                    cubicTo(
                        w * 0.64f, h * 0.74f,
                        w * 0.28f, h * 0.50f,
                        0f, h * 0.60f,
                    )
                    close()
                }
                drawPath(
                    path = emeraldSweep,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xB300B894), Color(0xCC55E67A), Color(0x99F9CA24)),
                        start = Offset(0f, h * 0.35f),
                        end = Offset(w, h * 0.68f),
                    ),
                )

                // Warm golden-amber sweep at lower third
                val warmSweep = Path().apply {
                    moveTo(0f, h * 0.62f)
                    cubicTo(
                        w * 0.35f, h * 0.54f,
                        w * 0.72f, h * 0.78f,
                        w, h * 0.66f,
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = warmSweep,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xCCF7B731), Color(0xE6FA8231), Color(0xEBEE5A24)),
                        start = Offset(w * 0.1f, h * 0.6f),
                        end = Offset(w * 0.9f, h),
                    ),
                )

                // Subtle radial highlight glow in upper-left for clock legibility
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x4474B9FF), Color.Transparent),
                        center = Offset(w * 0.25f, h * 0.22f),
                        radius = w * 0.65f,
                    ),
                )
            }
        }

        if (blurredScrim) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x88102A43),
                                Color(0x77164E63),
                                Color(0x885C3A16),
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
fun SystemDisplayOverlay(
    brightnessLevel: Float,
    readingMode: Boolean,
) {
    // Dimming overlay when brightness is lowered
    val dimAlpha = ((1f - brightnessLevel) * 0.62f).coerceIn(0f, 0.58f)
    if (dimAlpha > 0.01f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha)),
        )
    }
    if (readingMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ReadingTint),
        )
    }
}
