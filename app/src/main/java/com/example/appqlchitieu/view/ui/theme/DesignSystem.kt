package com.example.appqlchitieu.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppColors {
    // Gradient nền chung
    val GradientTop = Color(0xFF6A5ACD)   // tím indigo
    val GradientMid = Color(0xFF1976D2)   // xanh primary
    val GradientBottom = Color(0xFFE3F2FD) // nền sáng cuối

    // Brand
    val Primary = Color(0xFF1976D2)
    val Secondary = Color(0xFF388E3C)
    val Accent = Color(0xFF512DA8)

    // Text & surface
    val Card = Color.White
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
    val Divider = Color(0xFFE0E0E0)

    // Status
    val Danger = Color(0xFFE53935)
    val Warning = Color(0xFFFFA000)
    val Success = Color(0xFF43A047)

    // Neutral/Inputs
    val FieldBorder = Color(0xFFCFE0EB)
    val FieldFocus = Color(0xFF79C4F9)
    val FieldContainer = Color.White
    val ChipBg = Color(0xFFF7F7F7)
}

object AppDimens {
    val Screen = 16.dp
    val CardRadius = 24.dp
    val SmallRadius = 16.dp
    val ChipRadius = 20.dp

    val GapXS = 6.dp
    val GapS = 8.dp
    val GapM = 12.dp
    val GapL = 16.dp
    val GapXL = 24.dp
}
