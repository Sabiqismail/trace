package com.trace.journal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.trace.journal.R

val DMSerifDisplay = FontFamily(Font(R.font.dm_serif_display_regular))
val Inter = FontFamily(Font(R.font.inter_regular))

val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = DMSerifDisplay,
        fontSize = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontSize = 16.sp
    )
)
