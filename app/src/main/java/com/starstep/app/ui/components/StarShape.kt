package com.starstep.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.starstep.app.ui.theme.Gold
import com.starstep.app.ui.theme.Ink
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/** 五角星路径（立体感：主体 + 高光边） */
private fun starPath(cx: Float, cy: Float, r: Float): Path {
    val path = Path()
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) r else r * 0.45f
        val angle = (i * 36 - 90) * PI / 180.0
        val x = cx + radius * cos(angle).toFloat()
        val y = cy + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/**
 * 一颗立体五角星。可点击展开回执；长按触发拆星撤销入口。
 * floating 轻微漂浮由外部 infiniteRepeatable 动画驱动。
 */
@Composable
fun StarIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 48,
    contentDesc: String = "星星",
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val clickable = onClick != null || onLongClick != null
    Canvas(
        modifier = modifier
            .semantics { contentDescription = contentDesc }
            .then(
                if (clickable) Modifier.clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = { onClick?.invoke() }
                ) else Modifier
            )
    ) {
        val cx = size / 2f
        val cy = size / 2f
        val r = size * 0.46f
        // 主体
        drawPath(starPath(cx, cy, r), color)
        // 高光：左上三条边提亮
        drawPath(
            starPath(cx - size * 0.02f, cy - size * 0.03f, r * 0.97f),
            color.copy(alpha = 0.35f)
        )
        // 中心微光点
        drawCircle(color = Ink.copy(alpha = 0.55f), radius = size * 0.045f, center = Offset(cx, cy))
    }
}

/** 阶梯图形：用 N 颗小星铺成向上阶梯 */
@Composable
fun StarLadder(
    count: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = com.starstep.app.ui.theme.StarSky,
    inactiveColor: Color = com.starstep.app.ui.theme.InkFaint
) {
    Canvas(modifier = modifier) {
        val steps = 5
        val stepW = size.width / steps
        val stepH = size.height / steps
        for (i in 0 until steps) {
            val x = stepW * i + stepW / 2f
            val y = size.height - stepH * i - stepH / 2f
            val c = if (i < count.coerceAtMost(steps)) activeColor else inactiveColor
            drawPath(starPath(x, y, stepW * 0.32f), c)
        }
    }
}
