package com.starstep.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.AppState
import com.starstep.app.ui.components.MascotMood
import com.starstep.app.ui.components.PrimaryButton
import com.starstep.app.ui.components.SecondaryButton
import com.starstep.app.ui.components.SnowballMascot
import com.starstep.app.ui.components.StepProgressBar
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkFaint
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightLine
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.ui.theme.PowerUp
import com.starstep.app.viewmodel.StepResult
import com.starstep.app.viewmodel.StarStepViewModel
import kotlinx.coroutines.delay

/** 执行页：单步聚焦，屏幕只显示当前一步，减少决策负担 */
@Composable
fun ExecuteScreen(
    routeId: String,
    state: AppState,
    vm: StarStepViewModel,
    onFold: () -> Unit,
    onAdjust: () -> Unit,
    onBack: () -> Unit
) {
    val route = state.routes.firstOrNull { it.id == routeId }
    val session = state.session
    val showEnergyPicker = remember { mutableStateOf(false) }
    var selectedEnergy by remember { mutableIntStateOf(3) }

    // 若没有会话（可能从详情页直接进），自动开始
    LaunchedEffect(routeId) {
        if (session == null || session.routeId != routeId) {
            vm.startRoute(routeId)
        }
    }

    if (route == null || session == null || session.routeId != routeId) {
        Column(Modifier.fillMaxSize().background(NightBg).padding(20.dp), verticalArrangement = Arrangement.Center) {
            Text("正在准备路线…", color = InkSub)
        }
        return
    }

    val currentIndex = session.currentStep.coerceAtMost(route.steps.lastIndex)
    val step = route.steps[currentIndex]
    val total = route.steps.size
    val remaining = total - currentIndex - 1
    val progress = (currentIndex + 1).toFloat() / total.toFloat()
    val voiceOn = state.settings.voiceGuide
    val estimateMin = ((total - currentIndex) * 4).coerceAtLeast(1)

    // 本步计时：用时间戳计算 elapsed，切后台回来继续，不因离开清零
    var elapsed by remember(step.id) { mutableLongStateOf(0L) }
    LaunchedEffect(step.id, session.stepStartedAt) {
        while (true) {
            elapsed = (System.currentTimeMillis() - session.stepStartedAt) / 1000
            delay(1000)
        }
    }

    var actionBump by remember { mutableStateOf(false) }
    val mascotMood = if (actionBump) MascotMood.CHEER else MascotMood.HAPPY

    Column(
        Modifier
            .fillMaxSize()
            .background(NightBg)
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        // 顶栏
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Close, contentDescription = "关闭", tint = InkSub) }
            Text("${route.name} · 第 ${currentIndex + 1} / $total 步", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink, modifier = Modifier.weight(1f))
            IconButton(onClick = onAdjust) { Icon(Icons.Filled.Tune, contentDescription = "调整", tint = InkSub) }
        }

        Spacer(Modifier.height(12.dp))
        StepProgressBar(progress)
        Spacer(Modifier.height(8.dp))
        Text("还剩 $remaining 步 · 约 $estimateMin 分钟 · 语音引导${if (voiceOn) "已开" else "已关"}", fontSize = 12.sp, color = InkSub)

        Spacer(Modifier.height(28.dp))
        // 吉祥物 + 行动力反馈
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            SnowballMascot(mood = mascotMood, reduceMotion = state.settings.reduceMotion, size = 88)
            Spacer(Modifier.height(8.dp))
            val bumpScale by animateFloatAsState(if (actionBump) 1.25f else 1f, tween(220), label = "bump")
            Box(
                modifier = Modifier
                    .graphicsLayer { scaleX = bumpScale; scaleY = bumpScale }
                    .clip(RoundedCornerShape(16.dp))
                    .background(PowerUp.copy(alpha = 0.18f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("行动力 +2", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PowerUp)
            }
            Spacer(Modifier.height(4.dp))
            Text("累计行动力 ${state.actionPower}", fontSize = 11.sp, color = InkSub)
        }

        Spacer(Modifier.height(24.dp))
        // 当前步骤卡
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NightSurface)
                .padding(20.dp)
        ) {
            Text("现 在 这 一 步", fontSize = 11.sp, color = Accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(step.title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
            if (step.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(step.description, fontSize = 14.sp, color = InkSub)
            }
            Spacer(Modifier.height(16.dp))
            // 计时胶囊
            Box(Modifier.clip(RoundedCornerShape(14.dp)).background(NightLine).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text("本步 ${formatTime(elapsed)}", fontSize = 12.sp, color = Ink)
            }
        }

        Spacer(Modifier.height(20.dp))
        // 三工具图标
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ToolButton(Icons.Filled.Timer, "计时") { vm.emit(com.starstep.app.viewmodel.UiEvent.Toast("本步计时中 ${formatTime(elapsed)}")) }
            ToolButton(Icons.Filled.Mic, if (voiceOn) "语音开" else "语音关") { vm.toggleVoice() }
            ToolButton(Icons.Filled.SelfImprovement, "记状态") { showEnergyPicker.value = true }
        }

        Spacer(Modifier.weight(1f))
        // 主按钮：完成这一步
        PrimaryButton("完成这一步", onClick = {
            actionBump = true
            when (val result = vm.completeStep()) {
                is StepResult.NextStep -> {
                    actionBump = false
                }
                is StepResult.RouteFinished -> {
                    onFold()
                }
                StepResult.Error -> actionBump = false
            }
        })
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("调整路线", onClick = onAdjust, modifier = Modifier.weight(1f))
            SecondaryButton("遇到困难", onClick = {
                when (vm.skipStep()) {
                    is StepResult.RouteFinished -> onFold()
                    else -> Unit
                }
            }, modifier = Modifier.weight(1f))
        }
    }

    // 记状态弹层
    if (showEnergyPicker.value) {
        EnergyPickerDialog(
            selected = selectedEnergy,
            onSelect = { selectedEnergy = it },
            onConfirm = {
                vm.recordEnergyDuringExecute(selectedEnergy)
                showEnergyPicker.value = false
            },
            onDismiss = { showEnergyPicker.value = false }
        )
    }
}

@Composable
private fun ToolButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(NightSurface), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = Accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = InkSub)
    }
}

@Composable
private fun EnergyPickerDialog(
    selected: Int,
    onSelect: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NightSurface,
        title = { Text("记下现在的状态", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                com.starstep.app.data.EnergyLevels.options.forEach { (level, label) ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(if (selected == level) Accent.copy(alpha = 0.2f) else NightBg)
                            .clickable { onSelect(level) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, color = if (selected == level) Accent else Ink, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        },
        confirmButton = { androidx.compose.material3.TextButton(onClick = onConfirm) { Text("记下", color = Accent) } },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("取消", color = InkSub) } }
    )
}

private fun formatTime(sec: Long): String {
    val m = sec / 60
    val s = sec % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
