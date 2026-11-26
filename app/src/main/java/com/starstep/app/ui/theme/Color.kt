package com.starstep.app.ui.theme

import androidx.compose.ui.graphics.Color

// 夜空底色
val NightBg = Color(0xFF0D1B2A)
val NightSurface = Color(0xFF12263A)
val NightSurfaceHigh = Color(0xFF1B3A5C)
val NightLine = Color(0x2E8CAAD8)

// 文字
val Ink = Color(0xFFEAF2FA)
val InkSub = Color(0xFF8FA9C0)
val InkFaint = Color(0xFF5F7891)

// 主色 / 强调
val Deep = Color(0xFF2E5C8A)
val Accent = Color(0xFF5B8FD4)
val AccentSoft = Color(0x335B8FD4)

// 星星纸色卡
val StarSky = Color(0xFF7EC8E3)
val StarCream = Color(0xFFF5F3E7)
val StarMint = Color(0xFFA8D5BA)
val StarRose = Color(0xFFE8B4B8)
val StarDeep = Color(0xFF2E5C8A)

// 语义色：只做正反馈，无红色惩罚
val PowerUp = Color(0xFFA8D5BA)
val Gold = Color(0xFFF2C14E)

fun colorForKey(key: String): Color = when (key) {
    "sky" -> StarSky
    "cream" -> StarCream
    "mint" -> StarMint
    "rose" -> StarRose
    "deep" -> StarDeep
    else -> StarSky
}
