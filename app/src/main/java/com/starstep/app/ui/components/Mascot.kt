package com.starstep.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.StarSky

/**
 * 雪球精灵：一个圆润的雪球，带眨眼与轻微上下浮动。
 * mood 影响表情；reduceMotion 时关闭动画（尊重系统减少动态效果）。
 */
@Composable
fun SnowballMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.IDLE,
    reduceMotion: Boolean = false,
    size: Int = 96
) {
    val infinite = rememberInfiniteTransition(label = "mascot")
    val float by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    val blink by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (reduceMotion) 1f else 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, delayMillis = 2600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Canvas(
        modifier = modifier
            .semantics { contentDescription = "雪球精灵" }
    ) {
        val cx = size / 2f
        val cy = size / 2f + float * 4f
        val r = size * 0.42f

        // 雪球主体（晴空蓝渐变近似：主体 + 高光）
        drawCircle(color = StarSky, radius = r, center = Offset(cx, cy))
        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = r * 0.7f,
            center = Offset(cx - r * 0.22f, cy - r * 0.22f)
        )

        // 眼睛
        val eyeY = cy - r * 0.05f
        val eyeDx = r * 0.32f
        drawCircle(color = Ink.copy(alpha = 0.9f), radius = r * 0.07f * blink, center = Offset(cx - eyeDx, eyeY))
        drawCircle(color = Ink.copy(alpha = 0.9f), radius = r * 0.07f * blink, center = Offset(cx + eyeDx, eyeY))

        // 嘴：随情绪变化
        when (mood) {
            MascotMood.HAPPY -> {
                drawCircle(
                    color = Ink.copy(alpha = 0.85f),
                    radius = r * 0.13f,
                    center = Offset(cx, cy + r * 0.24f)
                )
            }
            MascotMood.CHEER -> {
                // 张嘴 + 腮红
                drawCircle(color = Ink.copy(alpha = 0.9f), radius = r * 0.16f, center = Offset(cx, cy + r * 0.26f))
                drawCircle(color = Color(0xFFE8B4B8).copy(alpha = 0.7f), radius = r * 0.1f, center = Offset(cx - r * 0.45f, cy + r * 0.18f))
                drawCircle(color = Color(0xFFE8B4B8).copy(alpha = 0.7f), radius = r * 0.1f, center = Offset(cx + r * 0.45f, cy + r * 0.18f))
            }
            MascotMood.IDLE -> {
                // 一条浅浅的弧线近似：小圆点横线
                drawCircle(color = Ink.copy(alpha = 0.7f), radius = r * 0.04f, center = Offset(cx, cy + r * 0.25f))
            }
        }
    }
}

enum class MascotMood { IDLE, HAPPY, CHEER }
