package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = CasinoPrimary,
    secondary = CasinoSecondary,
    tertiary = CasinoAccent,
    background = CasinoDarkBg,
    surface = CasinoCardBg,
    error = CasinoError,
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for sleek crypto casino look
  dynamicColor: Boolean = false, // Disable dynamic colors to keep crypto casino aesthetic consistent
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
