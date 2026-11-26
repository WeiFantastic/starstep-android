package com.starstep.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.AppState
import com.starstep.app.data.Star
import com.starstep.app.ui.components.StarIcon
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.PowerUp
import com.starstep.app.ui.theme.StarSky
import com.starstep.app.viewmodel.StarStepViewModel
import kotlinx.coroutines.delay

/**
 * 折星完成态：长纸条逐圈收拢为立体五角星（约1.5s）。
 * 点击跳过；系统减少动态效果时降级为淡入（200ms）落定。
 */
@Composable
fun FoldScreen(
    state: AppState,
    vm: StarStepViewModel,
    onStarWall: () -> Unit,
    onWriteNote: (String) -> Unit
) {
    val route = state.session?.routeId?.let { id -> state.routes.firstOrNull { it.id == id } }
    val hasValidSession = state.session != null && route != null
    var autoReturned by remember { mutableStateOf(false) }

    // 自愈：任何异常落到完成页但会话已失效（折星后会话被清空、旧数据残留等），
    // 自动返回星阶墙。杜绝「0/0 空完成页」卡在栈里、底部 Tab 切不出去的问题。
    LaunchedEffect(hasValidSession) {
        if (!hasValidSession && !autoReturned) {
            autoReturned = true
            onStarWall()
        }
    }

    if (!hasValidSession) {
        Column(
            Modifier.fillMaxSize().background(NightBg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("这条路线已收好，正在返回星阶墙…", color = InkSub, fontSize = 14.sp)
        }
        return
    }

    val color = com.starstep.app.ui.theme.colorForKey(route?.colorKey ?: "sky")
    val reduceMotion = state.settings.reduceMotion

    var animDone by remember { mutableStateOf(reduceMotion) }
    val foldProgress = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            foldProgress.snapTo(1f)
            animDone = true
        } else {
            foldProgress.animateTo(1f, animationSpec = tween(1500))
            animDone = true
        }
    }

    var foldedStar by remember { mutableStateOf<Star?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(NightBg)
            .clickable { // 动画中任意点击跳过
                if (!animDone) {
                    animDone = true
                }
            }
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))
        Text("这一小步，折成了", fontSize = 16.sp, color = InkSub)

        Spacer(Modifier.height(40.dp))
        // 折纸动画：纸条旋转收拢为星
        Box(
            Modifier
                .size(160.dp)
                .graphicsLayer {
                    rotationZ = rotation.value
                    scaleX = 0.3f + 0.7f * foldProgress.value
                    scaleY = 0.3f + 0.7f * foldProgress.value
                    alpha = foldProgress.value
                },
            contentAlignment = Alignment.Center
        ) {
            StarIcon(color = color, size = 150)
        }

        LaunchedEffect(foldProgress.value) {
            rotation.animateTo(foldProgress.value * 360f, animationSpec = tween(200))
        }

        Spacer(Modifier.height(32.dp))

        if (animDone) {
            Text("一颗星 ✦", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(10.dp))
            Text("行动力 +2 · 已走过 ${route?.steps?.size ?: 0} / ${route?.steps?.size ?: 0} 步", fontSize = 13.sp, color = PowerUp)
            Spacer(Modifier.height(28.dp))
            Text(
                "这条路线全部走完了。\n它没有消失——被你折进星阶里，\n以后还能展开看看今天写的话。",
                fontSize = 14.sp, color = InkSub, lineHeight = 24.sp
            )
            Spacer(Modifier.weight(1f))
            com.starstep.app.ui.components.PrimaryButton("收进星阶", onClick = {
                // 落定：折星写入本地（默认状态一般、无留言）
                val star = foldedStar ?: vm.foldStar("", 3)
                foldedStar = star
                onStarWall()
            })
            Spacer(Modifier.height(12.dp))
            com.starstep.app.ui.components.SecondaryButton("写一句给自己的话 →", onClick = {
                val star = foldedStar ?: vm.foldStar("", 3)
                foldedStar = star
                if (star != null) onWriteNote(star.id)
            }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Text("动画可点击跳过", fontSize = 11.sp, color = InkSub)
        } else {
            Spacer(Modifier.weight(1f))
            Text("折星中… 点击可跳过", fontSize = 12.sp, color = InkSub)
        }
        Spacer(Modifier.height(24.dp))
    }
}
