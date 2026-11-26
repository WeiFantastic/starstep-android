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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.data.AppState
import com.starstep.app.ui.components.SecondaryButton
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightBg
import com.starstep.app.ui.theme.NightSurface
import com.starstep.app.util.UpdateChecker
import com.starstep.app.viewmodel.StarStepViewModel

/** 设置：语音引导 / 到点自动开始 / 减少动态效果，均可关闭尊重掌控感 */
@Composable
fun SettingsScreen(state: AppState, vm: StarStepViewModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(NightBg).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Ink) }
            Text("设置", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }
        Spacer(Modifier.height(20.dp))

        ToggleRow("语音引导", "播报当前步骤，静音时降级为文字", state.settings.voiceGuide, { vm.toggleVoice() })
        ToggleRow("到点自动开始", "提醒到达时自动开始第一步 / 自动推进", state.settings.autoStart, { vm.toggleAutoStart() })
        ToggleRow("减少动态效果", "折星与展开动画降级为淡入", state.settings.reduceMotion, { vm.toggleReduceMotion() })

        Spacer(Modifier.height(8.dp))
        // 检查更新：手动触发，结果由 ViewModel 的 update 状态驱动弹窗
        ClickRow("检查更新", "查看是否有新版本", onClick = { vm.checkUpdate() })
        // 意见反馈：拉起 QQ；未安装 QQ 时回退到复制号码提示
        ClickRow("意见反馈", "QQ ${UpdateChecker.FEEDBACK_QQ} · 欢迎吐槽与建议", onClick = {
            vm.openUrl(UpdateChecker.FEEDBACK_URL)
        })

        Spacer(Modifier.weight(1f))
        SecondaryButton("恢复演示数据", onClick = {
            vm.resetAll()
            onBack()
        }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Text("星阶 StarStep · v0.2.0", fontSize = 11.sp, color = InkSub, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(24.dp))
    }
}

/** 可点击的设置行（右侧带箭头） */
@Composable
private fun ClickRow(title: String, desc: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NightSurface)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Accent)
            Spacer(Modifier.height(4.dp))
            Text(desc, fontSize = 12.sp, color = InkSub)
        }
        Text("›", fontSize = 22.sp, color = InkSub)
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun ToggleRow(title: String, desc: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NightSurface)
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(desc, fontSize = 12.sp, color = InkSub)
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = NightBg,
                checkedTrackColor = Accent,
                uncheckedThumbColor = InkSub,
                uncheckedTrackColor = NightBg
            )
        )
    }
    Spacer(Modifier.height(12.dp))
}
