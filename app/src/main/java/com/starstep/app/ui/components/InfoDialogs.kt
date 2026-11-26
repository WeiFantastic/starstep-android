package com.starstep.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.ui.theme.StarSky
import com.starstep.app.util.AppContent

/** 首次打开：3 步快速上手引导 */
@Composable
fun GuideDialog(onDismiss: () -> Unit) {
    SectionListDialog(
        title = "欢迎来到星阶 ✦",
        subtitle = "给低精力的你，3 步学会起步",
        sections = AppContent.GUIDE_STEPS,
        confirmText = "开始走",
        onDismiss = onDismiss
    )
}

/** 设置页：完整使用帮助 */
@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    SectionListDialog(
        title = "使用帮助",
        subtitle = "关于星阶，你想知道的都在这里",
        sections = AppContent.HELP_SECTIONS,
        confirmText = "知道了",
        onDismiss = onDismiss
    )
}

/** 更新后首次打开 / 设置页回看：最近更新说明 */
@Composable
fun WhatsNewDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NightSurface,
        title = {
            Column {
                Text("本次更新 · ${AppContent.WHAT_NEW_VERSION}", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("看看这次改了什么", color = InkSub, fontSize = 12.sp)
            }
        },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                AppContent.WHAT_NEW_ITEMS.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NightBg)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text("✦  $item", color = Ink, fontSize = 14.sp, lineHeight = 22.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("知道了", color = Accent) }
        }
    )
}

/** 通用：标题 + 副标题 + 若干「小标题 / 说明」条目的弹窗 */
@Composable
private fun SectionListDialog(
    title: String,
    subtitle: String,
    sections: List<Pair<String, String>>,
    confirmText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NightSurface,
        title = {
            Column {
                Text(title, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = InkSub, fontSize = 12.sp)
            }
        },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                sections.forEachIndexed { index, (heading, body) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NightBg)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Text("${index + 1}. $heading", color = StarSky, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text(body, color = Ink, fontSize = 13.sp, lineHeight = 21.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(confirmText, color = Accent) }
        }
    )
}

