package com.starstep.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.AppState
import com.starstep.app.ui.components.PrimaryButton
import com.starstep.app.ui.components.SecondaryButton
import com.starstep.app.ui.components.StarIcon
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightLine
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.ui.theme.StarSky
import com.starstep.app.ui.theme.colorForKey
import com.starstep.app.viewmodel.StarStepViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 星星回执（展开态）：只读，可折回、再做一次、拆星撤销 */
@Composable
fun ReceiptScreen(
    starId: String,
    state: AppState,
    vm: StarStepViewModel,
    onAgain: (String) -> Unit,
    onBack: () -> Unit
) {
    val star = state.stars.firstOrNull { it.id == starId }
    val scroll = rememberScrollState()
    val reduceMotion = state.settings.reduceMotion
    var unfolded by remember { mutableStateOf(reduceMotion) }
    val unfold = remember { Animatable(0f) }

    // 留言编辑弹窗状态
    var showNoteEditor by remember { mutableStateOf(false) }
    var noteDraft by remember { mutableStateOf(star?.note ?: "") }

    LaunchedEffect(Unit) {
        if (reduceMotion) unfold.snapTo(1f)
        else unfold.animateTo(1f, animationSpec = tween(1100))
        unfolded = true
    }

    if (star == null) {
        Column(Modifier.fillMaxSize().background(NightBg).padding(20.dp), verticalArrangement = Arrangement.Center) {
            Text("这颗星已经不在了", color = InkSub)
            Spacer(Modifier.height(16.dp))
            PrimaryButton("返回", onBack)
        }
        return
    }

    val color = colorForKey(star.colorKey)

    Column(Modifier.fillMaxSize().background(NightBg).verticalScroll(scroll).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Ink) }
            Text("展开这颗星", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }

        Spacer(Modifier.height(24.dp))
        // 展开的星星
        Box(
            Modifier.fillMaxWidth().graphicsLayer {
                scaleX = 0.6f + 0.4f * unfold.value
                scaleY = 0.6f + 0.4f * unfold.value
                alpha = unfold.value
            },
            contentAlignment = Alignment.Center
        ) {
            StarIcon(color = color, size = 120, contentDesc = "展开的星星")
        }

        Spacer(Modifier.height(28.dp))
        // 纸条本体：手写体风格，点一下即可补写/修改留言
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(NightSurface)
                .clickable {
                    noteDraft = star.note
                    showNoteEditor = true
                }
                .padding(24.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("当时写的", fontSize = 11.sp, color = InkSub, modifier = Modifier.weight(1f))
                Text("✎ 点击${if (star.note.isBlank()) "补写" else "编辑"}", fontSize = 11.sp, color = Accent)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (star.note.isNotBlank()) star.note else "（当时没有留言，点这里写一句）",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Italic,
                color = if (star.note.isNotBlank()) Ink else InkSub,
                lineHeight = 32.sp
            )
        }

        Spacer(Modifier.height(16.dp))
        // 完成信息带
        Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text("完成于 ${formatTime(star.finishedAt)} · 用时 ${star.durationMinutes} 分钟", fontSize = 13.sp, color = InkSub)
            Spacer(Modifier.height(4.dp))
            Text("当时状态：${star.energyLabel} · 走过 ${star.stepsTotal} 步", fontSize = 13.sp, color = InkSub)
        }

        Spacer(Modifier.height(20.dp))
        // 雪球回信
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(StarSky.copy(alpha = 0.12f)).padding(16.dp)
        ) {
            Text("雪球回信", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StarSky)
            Spacer(Modifier.height(8.dp))
            Text(star.reply, fontSize = 15.sp, color = Ink)
        }

        Spacer(Modifier.height(28.dp))
        PrimaryButton("再做一次", onClick = { onAgain(star.routeId) })
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("折回", onClick = onBack, modifier = Modifier.weight(1f))
            SecondaryButton("拆星撤销", onClick = {
                vm.undoFold(star)
                onBack()
            }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
    }

    // 留言编辑弹窗：真正可输入的输入框，保存后即时更新回执展示
    if (showNoteEditor) {
        AlertDialog(
            onDismissRequest = { showNoteEditor = false },
            containerColor = NightSurface,
            title = { Text("写一句给自己的话", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("记下当时或此刻想说的话，只有你看得到", color = InkSub, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("比如：慢慢来，先把第一题写完就好", color = InkSub, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = NightLine,
                            cursorColor = Accent
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateStarNote(star.id, noteDraft)
                    showNoteEditor = false
                }) { Text("保存", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showNoteEditor = false }) { Text("取消", color = InkSub) }
            }
        )
    }
}

private fun formatTime(ts: Long): String {
    val fmt = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
    return fmt.format(Date(ts))
}
