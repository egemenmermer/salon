package io.github.jqssun.airplay.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
@Composable
fun AirPlayTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary=Color(0xFFA7C7FF),background=Color(0xFF111315),
        surface=Color(0xFF1B1E24),onBackground=Color(0xFFF4F4F5),onSurface=Color(0xFFF4F4F5),
        secondary=Color(0xFF9BD0B0),error=Color(0xFFE8B4AD)),content=content)
}
