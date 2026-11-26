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
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.Step
import com.starstep.app.ui.components.CapsuleSelector
import com.starstep.app.ui.components.InfoCard
import com.starstep.app.ui.components.PrimaryButton
import com.starstep.app.ui.components.SecondaryButton
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightLine
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.viewmodel.StarStepViewModel
import com.starstep.app.viewmodel.UiEvent

/** 路线生成与拆解粒度：粗/细两档，逐步可编辑，生成即可播放 */
@Composable
fun GenerateScreen(
    vm: StarStepViewModel,
    onStartExecute: (String) -> Unit,
    onAdjust: (String) -> Unit,
    onBack: () -> Unit
) {
    val draft by vm.draft.collectAsState()
    val state by vm.state.collectAsState()
    val granularity = if (state?.settings?.granularity == "coarse") "粗一点（3-6步）" else "细一点（6-12步）"
    val scroll = rememberScrollState()

    if (draft == null) {
        Column(
            Modifier.fillMaxSize().background(NightBg).padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("没有待生成的草案，请返回首页输入。", color = InkSub, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            PrimaryButton("返回", onBack)
        }
        return
    }

    var route = draft!!
    val steps = remember(route.id) { mutableStateListOf<Step>().apply { addAll(route.steps) } }

    Column(
        Modifier
            .fillMaxSize()
            .background(NightBg)
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Ink)
            }
            Text("定制路线", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }

        Spacer(Modifier.height(8.dp))
        InfoCard {
            Text("系统为你归纳", fontSize = 12.sp, color = InkSub)
            Spacer(Modifier.height(6.dp))
            Text(route.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink)
        }

        Spacer(Modifier.height(18.dp))
        Text("拆解粒度", fontSize = 13.sp, color = InkSub)
        Spacer(Modifier.height(8.dp))
        CapsuleSelector(
            options = listOf("粗一点（3-6步）", "细一点（6-12步）"),
            selected = granularity,
            onSelect = {
                vm.setGranularity(if (it.startsWith("粗")) "coarse" else "fine")
                // 重新生成草案
                route = vm.prepareDraft("", route.name)
                steps.clear(); steps.addAll(route.steps)
            }
        )

        Spacer(Modifier.height(20.dp))
        Text("生成预览 · 每步可直接改", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(10.dp))

        steps.forEachIndexed { index, step ->
            StepDraftRow(
                index = index + 1,
                step = step,
                onEdit = { newTitle ->
                    steps[index] = step.copy(title = newTitle)
                },
                onRemove = {
                    if (steps.size > 1) steps.removeAt(index)
                    else vm.emit(UiEvent.Toast("至少保留一步"))
                }
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(8.dp))
        SecondaryButton("+ 手动添加一步", onClick = {
            if (steps.size >= 12) {
                vm.emit(UiEvent.Toast("一步是不是可以再合并一下"))
            } else {
                steps.add(Step("step_${System.currentTimeMillis()}", "新的一步", durationSec = 60))
            }
        })

        Spacer(Modifier.height(28.dp))
        PrimaryButton("就走这条", onClick = {
            val finalRoute = route.copy(steps = steps.toList())
            vm.saveRoute(finalRoute)
            vm.startRoute(finalRoute.id)
            vm.clearDraft()
            onStartExecute(finalRoute.id)
        })
        Spacer(Modifier.height(12.dp))
        SecondaryButton("调整一下", onClick = {
            val finalRoute = route.copy(steps = steps.toList())
            vm.saveRoute(finalRoute)
            onAdjust(finalRoute.id)
        }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StepDraftRow(index: Int, step: Step, onEdit: (String) -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NightSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$index", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Accent)
        }
        Spacer(Modifier.padding(8.dp))
        OutlinedTextField(
            value = step.title,
            onValueChange = onEdit,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedBorderColor = NightLine,
                unfocusedBorderColor = NightLine,
                cursorColor = Accent
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
    }
}
