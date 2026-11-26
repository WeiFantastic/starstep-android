package com.starstep.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starstep.app.ui.theme.Accent
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.theme.InkSub
import com.starstep.app.ui.theme.NightSurface

data class TabItem(val key: String, val label: String, val icon: ImageVector)

val TAB_ITEMS = listOf(
    TabItem("home", "今天", Icons.Filled.Home),
    TabItem("starwall", "星阶", Icons.Filled.AutoAwesome),
    TabItem("routes", "路线", Icons.Filled.DateRange),
    TabItem("energy", "状态", Icons.Filled.MonitorHeart)
)

/** 底部四 Tab：今天 / 星阶 / 路线 / 状态 */
@Composable
fun BottomTabBar(
    current: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(NightSurface),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TAB_ITEMS.forEach { tab ->
            val selected = tab.key == current
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(tab.key) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp, 26.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(13.dp))
                        .background(if (selected) Accent.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) Accent else InkSub,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    tab.label,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) Ink else InkSub
                )
            }
        }
    }
}
