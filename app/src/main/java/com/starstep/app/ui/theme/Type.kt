package com.starstep.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** 字号遵循 M3 规范，中文显示优先系统字体 */
val StarStepTypography = Typography(
    displayLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default, color = Ink),
    displayMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default, color = Ink),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Default, color = Ink),
    headlineSmall = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Default, color = Ink),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Default, color = Ink),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Default, color = Ink),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Default, color = Ink),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Default, color = Ink),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Default, color = InkSub),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Default, color = Ink),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Default, color = InkSub),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Default, color = InkSub)
)
