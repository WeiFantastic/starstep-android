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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.AppState
import com.starstep.app.data.StarRanks
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.ui.theme.StarSky
import com.starstep.app.ui.theme.colorForKey

/** 我的路线列表：常走路线在前，一键重启是召回核心入口 */
@Composable
fun RoutesScreen(
    state: AppState,
    onOpenRoute: (String) -> Unit,
    onExecute: (String) -> Unit,
    onNew: () -> Unit
) {
    val sorted = state.routes.sortedByDescending { it.lastDoneAt }
    val rank = StarRanks.rankOf(state.stars.size)

    Box(Modifier.fillMaxSize().background(NightBg)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Text("我的路线", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(12.dp))
            // 统计胶囊
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill("${state.routes.size} 条路线")
                StatPill("已折 ${state.stars.size} 颗星 · 第${rank.level}阶")
            }
            Spacer(Modifier.height(16.dp))

            if (sorted.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("还没有路线，去首页定制一条", color = InkSub, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(sorted, key = { it.id }) { route ->
                        RouteCard(
                            name = route.name,
                            meta = "预估 ${route.estimatedMinutes} 分钟 · ${route.totalCompletions} 次",
                            time = if (route.reminderTime.isNotBlank()) route.reminderTime else "上次 ${timeAgo(route.lastDoneAt)}",
                            color = colorForKey(route.colorKey),
                            onClick = { onOpenRoute(route.id) },
                            onPlay = { onExecute(route.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
        // 悬浮新建按钮
        FloatingActionButton(
            onClick = onNew,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Accent,
            contentColor = NightBg
        ) {
            Icon(Icons.Filled.Add, contentDescription = "新建路线")
        }
    }
}

@Composable
private fun StatPill(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        color = Ink,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NightSurface)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

@Composable
private fun RouteCard(
    name: String, meta: String, time: String, color: Color,
    onClick: () -> Unit, onPlay: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NightSurface)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(color))
            Spacer(Modifier.padding(8.dp))
            Text(name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Ink, modifier = Modifier.weight(1f))
            IconButton(onClick = onPlay) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "开始", tint = StarSky)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(meta, fontSize = 12.sp, color = InkSub)
            Text(time, fontSize = 12.sp, color = InkSub)
        }
    }
}

private fun timeAgo(ts: Long): String {
    if (ts <= 0L) return "—"
    val diff = System.currentTimeMillis() - ts
    val days = diff / 86_400_000L
    return when {
        days <= 0 -> "今天"
        days == 1L -> "昨天"
        days < 7 -> "$days 天前"
        else -> "${days / 7} 周前"
    }
}
