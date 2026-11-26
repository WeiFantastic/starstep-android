package com.starstep.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.AppState
import com.starstep.app.data.EnergyLevels
import com.starstep.app.ui.components.InfoCard
import com.starstep.app.ui.components.PrimaryButton
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.ui.theme.StarSky
import com.starstep.app.viewmodel.StarStepViewModel
import java.util.Calendar

/** 精力状态：近 7 天曲线 + 洞察卡 + 四档打卡，数据三处汇聚 */
@Composable
fun EnergyScreen(state: AppState, vm: StarStepViewModel) {
    val scroll = rememberScrollState()
    var selectedLevel by remember { mutableIntStateOf(3) }
    val insight = vm.energyInsight()

    // 近 7 天聚合（按天取平均）
    val dailyLevels = remember(state.energyRecords) {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_YEAR)
        (6 downTo 0).map { d ->
            val targetDay = today - d
            val levels = state.energyRecords.filter {
                val c = Calendar.getInstance()
                c.timeInMillis = it.at
                c.get(Calendar.DAY_OF_YEAR) == targetDay
            }.map { it.level }
            if (levels.isEmpty()) 0.0 else levels.average()
        }
    }
    val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

    Column(Modifier.fillMaxSize().background(NightBg).verticalScroll(scroll).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("我的状态", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(4.dp))
        Text("你记录了多少次，就有多准", fontSize = 13.sp, color = InkSub)

        Spacer(Modifier.height(20.dp))
        InfoCard {
            Text("近 7 天精力曲线", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.height(16.dp))
            EnergyChart(dailyLevels, dayLabels)
        }

        Spacer(Modifier.height(16.dp))
        // 洞察卡
        if (insight != null) {
            InfoCard {
                Text("你的节奏", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StarSky)
                Spacer(Modifier.height(8.dp))
                Text(
                    "午后到傍晚是你的高能时段（${pad(insight.startHour)}:00–${pad(insight.endHour)}:00 平均精力 ${fmt(insight.avgLevel)}/5）。把「启动最难」的路线排到这里，会更省力。上午不必强求。",
                    fontSize = 14.sp, color = Ink, lineHeight = 24.sp
                )
            }
        } else {
            InfoCard {
                Text("你的节奏", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkSub)
                Spacer(Modifier.height(8.dp))
                Text("记录得越多，这里越准", fontSize = 14.sp, color = InkSub)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("今天感觉怎么样？", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(12.dp))
        // 四档情绪
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EnergyLevels.options.forEach { (level, label) ->
                val emoji = when (level) { 1 -> "😮‍💨"; 2 -> "😐"; 3 -> "🙂"; else -> "🤩" }
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selectedLevel == level) Accent.copy(alpha = 0.25f) else NightSurface)
                        .clickable { selectedLevel = level }
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(emoji, fontSize = 24.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(label, fontSize = 12.sp, color = if (selectedLevel == level) Accent else InkSub)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        PrimaryButton("记入精力周期", onClick = { vm.checkIn(selectedLevel) })
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EnergyChart(levels: List<Double>, labels: List<String>) {
    val maxLevel = 4.0
    Row(Modifier.fillMaxWidth().height(140.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceEvenly) {
        levels.forEachIndexed { index, level ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                val ratio = (level / maxLevel).toFloat().coerceIn(0f, 1f)
                Box(
                    Modifier
                        .fillMaxWidth(0.5f)
                        .height(if (level == 0.0) 4.dp else (100 * ratio).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (level == 0.0) Color(0x335B8FD4) else StarSky)
                )
                Spacer(Modifier.height(8.dp))
                Text(labels[index], fontSize = 11.sp, color = InkSub)
            }
        }
    }
}

private fun fmt(v: Double) = String.format("%.1f", v)
private fun pad(h: Int) = h.toString().padStart(2, '0')
