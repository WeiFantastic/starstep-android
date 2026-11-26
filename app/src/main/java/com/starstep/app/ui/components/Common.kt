package com.starstep.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Deep
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkFaint
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightLine
import com.starstep.app.ui.theme.NightSurface

/** 主按钮：蓝色高亮，圆角 28，56dp 高 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = com.starstep.app.ui.theme.NightBg,
            disabledContainerColor = Accent.copy(alpha = 0.3f),
            disabledContentColor = InkFaint
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** 次级按钮：描边 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp)),
        border = BorderStroke(1.dp, NightLine),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            SpacerW(6)
        }
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun SpacerW(w: Int) {
    androidx.compose.foundation.layout.Spacer(Modifier.size(w.dp))
}

/** 胶囊选择器：粗一点 / 细一点 */
@Composable
fun CapsuleSelector(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NightSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { opt ->
            val isSel = opt == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSel) Accent else Color.Transparent)
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    opt,
                    fontSize = 13.sp,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSel) com.starstep.app.ui.theme.NightBg else InkSub
                )
            }
        }
    }
}

/** 通用信息卡片 */
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NightSurface),
        border = BorderStroke(1.dp, NightLine)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/** 进度条 */
@Composable
fun StepProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        color = Accent,
        trackColor = NightLine
    )
}

/** 章节小标题 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.fillMaxWidth(),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = InkSub,
        textAlign = TextAlign.Start
    )
}

/** 顶部渐隐遮罩背景容器 */
@Composable
fun ScreenBackground(content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        com.starstep.app.ui.theme.NightBg,
                        com.starstep.app.ui.theme.NightSurface
                    )
                )
            )
    ) {
        content()
    }
}

/** Snackbar 提示宿主 */
@Composable
fun ToastHost(message: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Deep,
                    contentColor = Ink
                ) { Text(message) }
            }
        }
    }
}
