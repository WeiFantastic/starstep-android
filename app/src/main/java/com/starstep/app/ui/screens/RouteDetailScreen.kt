package com.starstep.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.AppState
import com.starstep.app.ui.components.InfoCard
import com.starstep.app.ui.components.PrimaryButton
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.viewmodel.StarStepViewModel

/** 路线详情：步骤清单 + 提醒规则 + 数据四宫格，全流程预估前置 */
@Composable
fun RouteDetailScreen(
    routeId: String,
    state: AppState,
    vm: StarStepViewModel,
    onExecute: () -> Unit,
    onAdjust: () -> Unit,
    onBack: () -> Unit
) {
    val route = state.routes.firstOrNull { it.id == routeId }
    val scroll = rememberScrollState()

    if (route == null) {
        Column(Modifier.fillMaxSize().background(NightBg).padding(20.dp), verticalArrangement = Arrangement.Center) {
            Text("路线不存在", color = InkSub)
            Spacer(Modifier.height(16.dp))
            PrimaryButton("返回", onBack)
        }
        return
    }

    val avgDuration = route.recentDurations.average().let { if (it.isNaN()) route.estimatedMinutes else it.toInt() }

    Column(
        Modifier.fillMaxSize().background(NightBg).verticalScroll(scroll).padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Ink) }
            Text(route.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.weight(1f))
            IconButton(onClick = onAdjust) { Icon(Icons.Filled.MoreVert, contentDescription = "调整", tint = InkSub) }
        }

        Spacer(Modifier.height(12.dp))
        Text("步骤 · 点击可展开", fontSize = 13.sp, color = InkSub)
        Spacer(Modifier.height(8.dp))
        route.steps.forEachIndexed { index, step ->
            StepExpandable(index + 1, step.title, step.description)
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(16.dp))
        InfoCard {
            Text("提醒规则", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.height(8.dp))
            val daysText = if (route.reminderDays.isEmpty()) "未设置" else route.reminderDays.joinToString("、") { dayName(it) }
            Text("$daysText ${route.reminderTime}", fontSize = 14.sp, color = InkSub)
            if (route.autoStart) {
                Spacer(Modifier.height(6.dp))
                Text("到点自动开始第一步", fontSize = 12.sp, color = Accent)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("这一路，你走过的", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(10.dp))
        // 数据四宫格
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DataCell("最近一次用时", if (route.recentDurations.isNotEmpty()) "${route.recentDurations.first()} 分钟" else "待积累", Modifier.weight(1f))
            DataCell("预估下次用时", if (route.recentDurations.isNotEmpty()) "$avgDuration 分钟" else "待积累", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DataCell("累计次数", "${route.totalCompletions} 次", Modifier.weight(1f))
            DataCell("全程预估", "${route.steps.size} 步 / ${route.estimatedMinutes}分", Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))
        PrimaryButton("走这条路", onExecute)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StepExpandable(index: Int, title: String, desc: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NightSurface)
            .clickable { if (desc.isNotBlank()) expanded = !expanded }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(26.dp).clip(RoundedCornerShape(13.dp)).background(Accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) { Text("${index.toString().padStart(2, '0')}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Accent) }
            Spacer(Modifier.padding(8.dp))
            Text(title, fontSize = 14.sp, color = Ink)
        }
        AnimatedVisibility(expanded) {
            if (desc.isNotBlank()) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(desc, fontSize = 12.sp, color = InkSub, modifier = Modifier.padding(start = 34.dp))
                }
            }
        }
    }
}

@Composable
private fun DataCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(NightSurface).padding(14.dp)
    ) {
        Text(label, fontSize = 12.sp, color = InkSub)
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Ink)
    }
}

private fun dayName(d: Int) = when (d) {
    1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"; 5 -> "周五"; 6 -> "周六"; 7 -> "周日"
    else -> "周$d"
}
