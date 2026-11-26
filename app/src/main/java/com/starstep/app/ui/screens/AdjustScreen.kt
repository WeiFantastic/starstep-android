package com.starstep.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
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
import com.starstep.app.data.Step
import com.starstep.app.ui.components.CapsuleSelector
import com.starstep.app.ui.components.InfoCard
import com.starstep.app.ui.components.PrimaryButton
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightLine
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.viewmodel.StarStepViewModel
import com.starstep.app.viewmodel.UiEvent

/** 调整路线：执行现场也能改，已完成步保留，改动只影响当前路线 */
@Composable
fun AdjustScreen(
    routeId: String,
    state: AppState,
    vm: StarStepViewModel,
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

    val steps = remember(routeId) { mutableStateListOf<Step>().apply { addAll(route.steps) } }
    var viewMode by remember { mutableStateOf<String>("聚焦当前步") }

    Column(Modifier.fillMaxSize().background(NightBg).verticalScroll(scroll).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Ink) }
            Text("调整路线", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }

        Spacer(Modifier.height(12.dp))
        // 当前步卡
        InfoCard {
            Text("当前步骤", fontSize = 11.sp, color = Accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val current = steps.firstOrNull()?.title ?: "—"
            Text(current, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Ink)
            Spacer(Modifier.height(10.dp))
            // 方案胶囊（示例：自己买 / 自己做）
            CapsuleSelector(options = listOf("① 自己买", "② 自己做"), selected = "① 自己买", onSelect = {})
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = steps.firstOrNull()?.title ?: "",
                onValueChange = { v -> if (steps.isNotEmpty()) steps[0] = steps[0].copy(title = v) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Ink, unfocusedTextColor = Ink,
                    focusedBorderColor = Accent, unfocusedBorderColor = NightLine, cursorColor = Accent
                ),
                label = { Text("点击可语音改这一步", fontSize = 12.sp) }
            )
        }

        Spacer(Modifier.height(16.dp))
        // 视图切换
        CapsuleSelector(options = listOf("聚焦当前步", "全览全部步骤"), selected = viewMode, onSelect = { viewMode = it })
        Spacer(Modifier.height(8.dp))
        Text("拖动排序 · 逐条改", fontSize = 11.sp, color = InkSub)
        Spacer(Modifier.height(10.dp))

        steps.forEachIndexed { index, step ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(NightSurface).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${index + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Accent)
                Spacer(Modifier.padding(8.dp))
                OutlinedTextField(
                    value = step.title,
                    onValueChange = { steps[index] = step.copy(title = it) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Ink, unfocusedTextColor = Ink,
                        focusedBorderColor = NightLine, unfocusedBorderColor = NightLine, cursorColor = Accent
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(20.dp))
        PrimaryButton("保存调整", onClick = {
            if (steps.isEmpty()) {
                vm.emit(UiEvent.Toast("至少保留一步"))
            } else {
                vm.saveAdjustedSteps(routeId, steps.toList())
                onBack()
            }
        })
        Spacer(Modifier.height(24.dp))
    }
}
