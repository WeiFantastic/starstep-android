package com.starstep.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.AppState
import com.starstep.app.data.StarColors
import com.starstep.app.data.StarRanks
import com.starstep.app.ui.components.CapsuleSelector
import com.starstep.app.ui.components.PrimaryButton
import com.starstep.app.ui.components.StarIcon
import com.starstep.app.ui.components.StarLadder
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Gold
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkFaint
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.ui.theme.colorForKey
import com.starstep.app.viewmodel.StarStepViewModel
import com.starstep.app.viewmodel.UiEvent

/** 星阶陈列墙：产品主线，成果长期可见，只增不减 */
@Composable
fun StarWallScreen(
    state: AppState,
    vm: StarStepViewModel,
    onOpenStar: (String) -> Unit
) {
    var filter by remember { mutableStateOf("全部") }
    val rank = StarRanks.rankOf(state.stars.size)
    val next = StarRanks.nextRank(state.stars.size)
    val remain = next?.let { it.threshold - state.stars.size } ?: 0

    val filtered = if (filter == "全部") state.stars else state.stars.filter { it.colorKey == filter }

    Column(Modifier.fillMaxSize().background(NightBg).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("我的星阶", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(4.dp))
        Text("你走过的路，铺成了阶梯", fontSize = 13.sp, color = InkSub)

        Spacer(Modifier.height(18.dp))
        // 统计区
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(NightSurface).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("${state.stars.size} 颗星 · 第 ${rank.level} 阶", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(6.dp))
                if (next != null) {
                    Text("再折 $remain 颗，升到第 ${next.level} 阶「${next.name}」", fontSize = 12.sp, color = Accent)
                } else {
                    Text("已到最高阶，继续闪光 ✦", fontSize = 12.sp, color = Gold)
                }
            }
            StarLadder(count = rank.level, modifier = Modifier.size(80.dp, 56.dp))
        }

        Spacer(Modifier.height(16.dp))
        // 色卡筛选
        CapsuleSelector(
            options = StarColors.filterOptions.map { StarColors.filterLabel[it] ?: it },
            selected = StarColors.filterLabel[filter] ?: filter,
            onSelect = { label ->
                filter = StarColors.filterLabel.entries.firstOrNull { it.value == label }?.key ?: "全部"
            }
        )

        Spacer(Modifier.height(12.dp))
        Text("单击星星展开回执 · 长按可拆星撤销", fontSize = 11.sp, color = InkFaint)

        Spacer(Modifier.height(8.dp))
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("这里还没有星星\n折下第一颗，从这里开始", fontSize = 14.sp, color = InkSub, lineHeight = 24.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(filtered, key = { it.id }) { star ->
                    FloatingStar(
                        color = colorForKey(star.colorKey),
                        index = state.stars.indexOf(star),
                        reduceMotion = state.settings.reduceMotion,
                        onClick = { onOpenStar(star.id) },
                        onLongClick = {
                            vm.undoFold(star)
                            vm.emit(UiEvent.Toast("已拆回，随时可以再来"))
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        PrimaryButton("展开最近一颗", onClick = {
            state.stars.firstOrNull()?.let { onOpenStar(it.id) }
                ?: vm.emit(UiEvent.Toast("还没有星星"))
        })
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FloatingStar(
    color: androidx.compose.ui.graphics.Color,
    index: Int,
    reduceMotion: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "float$index")
    val float by infinite.animateFloat(
        initialValue = -3f,
        targetValue = if (reduceMotion) -3f else 3f,
        animationSpec = infiniteRepeatable(tween(1800 + (index % 5) * 200), RepeatMode.Reverse),
        label = "floatVal"
    )
    Box(
        Modifier
            .size(64.dp)
            .graphicsLayer { translationY = float }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        StarIcon(color = color, size = 56, contentDesc = "星星，点击查看回执")
    }
}
