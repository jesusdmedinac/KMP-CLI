package org.example.project.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object ZoraColors {
    val Purple = Color(0xFF894CFF)
    val Violet = Color(0xFFA833FD)
    val Magenta = Color(0xFFCD00EB)
    val Fuchsia = Color(0xFFE200C0)
    val Pink = Color(0xFFEC168A)

    val DarkBackground = Color(0xFF000000)
    val LightBackground = Color(0xFFFFFFFF)

    val BrandGradient = Brush.linearGradient(
        colors = listOf(Purple, Violet, Magenta, Fuchsia, Pink)
    )
}
