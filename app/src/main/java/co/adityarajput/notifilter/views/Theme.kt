package co.adityarajput.notifilter.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import co.adityarajput.notifilter.R

private val ColorScheme = darkColorScheme(
    primary = Color(0xFF7C70FF), // Purple
    secondary = Color(0xFF70FFA4), // Green
    tertiary = Color(0xFFFF7070), // Red
)

private val Typography = Typography().run {
    val firaMono = FontFamily(
        Font(R.font.firamono_regular, FontWeight.Normal),
        Font(R.font.firamono_medium, FontWeight.Medium),
        Font(R.font.firamono_bold, FontWeight.Bold),
    )

    Typography(
        displayLarge.copy(fontFamily = firaMono),
        displayMedium.copy(fontFamily = firaMono),
        displaySmall.copy(fontFamily = firaMono),
        headlineLarge.copy(fontFamily = firaMono),
        headlineMedium.copy(fontFamily = firaMono),
        headlineSmall.copy(fontFamily = firaMono),
        titleLarge.copy(fontFamily = firaMono),
        titleMedium.copy(fontFamily = firaMono),
        titleSmall.copy(fontFamily = firaMono),
        bodyLarge.copy(fontFamily = firaMono),
        bodyMedium.copy(fontFamily = firaMono),
        bodySmall.copy(fontFamily = firaMono),
        labelLarge.copy(fontFamily = firaMono),
        labelMedium.copy(fontFamily = firaMono),
        labelSmall.copy(fontFamily = firaMono),
    )
}

@Composable
fun Theme(content: @Composable () -> Unit) =
    MaterialTheme(ColorScheme, MaterialTheme.shapes, Typography, content)
