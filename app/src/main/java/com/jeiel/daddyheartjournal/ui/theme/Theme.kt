package com.jeiel.daddyheartjournal.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryEspresso,
    secondary = SecondaryTerracotta,
    tertiary = SlateNavy,
    background = WarmDarkText,
    surface = Color(0xFF332922),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = SoftCreamBg,
    onSurface = SoftCreamBg
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryEspresso,
    secondary = SecondaryTerracotta,
    tertiary = SlateNavy,
    background = SoftCreamBg,
    surface = SoftCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = WarmDarkText,
    onSurface = WarmDarkText
)

@Composable
fun DaddyHeartJournalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic colors to enforce our beautiful cohesive warm diary palette!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

