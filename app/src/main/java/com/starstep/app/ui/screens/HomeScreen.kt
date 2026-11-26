package com.starstep.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.AppState
import com.starstep.app.data.StarColors
import com.starstep.app.ui.components.CapsuleSelector
import com.starstep.app.ui.components.InfoCard
import com.starstep.app.ui.components.MascotMood
import com.starstep.app.ui.components.PrimaryButton
import com.starstep.app.ui.components.SecondaryButton
import com.starstep.app.ui.components.SnowballMascot
import com.starstep.app.ui.components.SpacerW
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkFaint
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightLine
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.ui.theme.StarSky
import com.starstep.app.ui.theme.colorForKey
import com.starstep.app.viewmodel.StarStepViewModel
import com.starstep.app.viewmodel.UiEvent

/** 首页：意图捕获（现状 → 目标），双输入是路线生成的唯一输入源 */
@Composable
fun HomeScreen(
    state: AppState,
    vm: StarStepViewModel,
    onGenerate: () -> Unit,
    onOpenRoute: (String) -> Unit,
    onExecute: (String) -> Unit,
    onSettings: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    val scroll = rememberScrollState()
    val granularity = if (state.settings.granularity == "coarse") "今天粗一点" else "细一点"
    val recentRoutes = state.routes.sortedByDescending { it.lastDoneAt }.take(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBg)
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        // 顶部标题 + 设置
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("星阶", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "设置", tint = InkSub)
            }
        }

        Spacer(Modifier.height(8.dp))
        // 吉祥物区
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            SnowballMascot(
                mood = MascotMood.IDLE,
                reduceMotion = state.settings.reduceMotion,
                size = 96
            )
            Spacer(Modifier.height(8.dp))
            Text("今天，从哪里开始？", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("先做一件事，就折一颗星", fontSize = 13.sp, color = InkSub)
        }

        Spacer(Modifier.height(24.dp))
        // 双输入框
        IntentField(
            value = current,
            onValueChange = { current = it },
            label = "我现在在做什么",
            placeholder = "比如在刷手机"
        )
        Spacer(Modifier.height(12.dp))
        IntentField(
            value = goal,
            onValueChange = { goal = it },
            label = "我想做什么",
            placeholder = "比如想写一份报告"
        )

        Spacer(Modifier.height(16.dp))
        // 次级按钮组
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(
                "澄清意图",
                onClick = {
                    if (current.isBlank() && goal.isBlank()) {
                        vm.emit(UiEvent.Toast("先说说你想做什么吧"))
                    } else {
                        vm.prepareDraft(current, goal)
                        onGenerate()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            SecondaryButton(
                "定制路线",
                onClick = {
                    if (current.isBlank() && goal.isBlank()) {
                        vm.emit(UiEvent.Toast("先说说你想做什么吧"))
                    } else {
                        vm.prepareDraft(current, goal)
                        onGenerate()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))
        // 粒度选择
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("拆解粒度：", fontSize = 13.sp, color = InkSub)
            CapsuleSelector(
                options = listOf("今天粗一点", "细一点"),
                selected = granularity,
                onSelect = { vm.setGranularity(if (it == "今天粗一点") "coarse" else "fine") }
            )
        }

        Spacer(Modifier.height(20.dp))
        PrimaryButton("滚动起来吧", onClick = {
            if (current.isBlank() && goal.isBlank()) {
                vm.emit(UiEvent.Toast("先说说你想做什么吧"))
                return@PrimaryButton
            }
            val route = vm.prepareDraft(current, goal)
            vm.saveRoute(route)
            vm.startRoute(route.id)
            onExecute(route.id)
        })

        Spacer(Modifier.height(28.dp))
        // 最近的路
        Text("最近的路", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(10.dp))
        if (recentRoutes.isEmpty()) {
            InfoCard {
                Text("完成第一条路线后会出现在这里", fontSize = 13.sp, color = InkSub)
            }
        } else {
            recentRoutes.forEach { route ->
                RecentRouteCard(
                    name = route.name,
                    meta = "${route.estimatedMinutes}分钟" +
                            (if (route.streakDays > 0) " · 连走 ${route.streakDays} 天" else ""),
                    color = colorForKey(route.colorKey),
                    onClick = { onOpenRoute(route.id) },
                    onPlay = { onExecute(route.id) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun IntentField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = { Text(placeholder, fontSize = 13.sp, color = InkFaint) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            focusedBorderColor = Accent,
            unfocusedBorderColor = NightLine,
            focusedLabelColor = Accent,
            unfocusedLabelColor = InkSub,
            cursorColor = Accent
        )
    )
}

@Composable
private fun RecentRouteCard(
    name: String,
    meta: String,
    color: Color,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NightSurface)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color)
        )
        SpacerW(12)
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(meta, fontSize = 12.sp, color = InkSub)
        }
        IconButton(onClick = onPlay) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "开始", tint = StarSky)
        }
    }
}
