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

@Composable
fun Theme(content: @Composable () -> Unit) {
    val firaMono = FontFamily(
        Font(R.font.firamono_regular, FontWeight.Normal),
        Font(R.font.firamono_medium, FontWeight.Medium),
        Font(R.font.firamono_bold, FontWeight.Bold),
    )

    val defaultTypography = Typography()

    MaterialTheme(
        darkColorScheme(
            primary = Color(0xFF7C70FF), // Purple
            secondary = Color(0xFF70FFA4), // Green
            tertiary = Color(0xFFFF7070), // Red
        ),
        MaterialTheme.shapes,
        Typography(
            defaultTypography.displayLarge.copy(fontFamily = firaMono),
            defaultTypography.displayMedium.copy(fontFamily = firaMono),
            defaultTypography.displaySmall.copy(fontFamily = firaMono),
            defaultTypography.headlineLarge.copy(fontFamily = firaMono),
            defaultTypography.headlineMedium.copy(fontFamily = firaMono),
            defaultTypography.headlineSmall.copy(fontFamily = firaMono),
            defaultTypography.titleLarge.copy(fontFamily = firaMono),
            defaultTypography.titleMedium.copy(fontFamily = firaMono),
            defaultTypography.titleSmall.copy(fontFamily = firaMono),
            defaultTypography.bodyLarge.copy(fontFamily = firaMono),
            defaultTypography.bodyMedium.copy(fontFamily = firaMono),
            defaultTypography.bodySmall.copy(fontFamily = firaMono),
            defaultTypography.labelLarge.copy(fontFamily = firaMono),
            defaultTypography.labelMedium.copy(fontFamily = firaMono),
            defaultTypography.labelSmall.copy(fontFamily = firaMono),
        ),
        content,
    )
}
