package co.adityarajput.notifilter.views.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val History: ImageVector
    get() {
        if (_History != null) return _History!!

        _History = ImageVector.Builder(
            name = "History",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(480f, 840f)
                quadToRelative(-138f, 0f, -240.5f, -91.5f)
                reflectiveQuadTo(122f, 520f)
                horizontalLineToRelative(82f)
                quadToRelative(14f, 104f, 92.5f, 172f)
                reflectiveQuadTo(480f, 760f)
                quadToRelative(117f, 0f, 198.5f, -81.5f)
                reflectiveQuadTo(760f, 480f)
                reflectiveQuadToRelative(-81.5f, -198.5f)
                reflectiveQuadTo(480f, 200f)
                quadToRelative(-69f, 0f, -129f, 32f)
                reflectiveQuadToRelative(-101f, 88f)
                horizontalLineToRelative(110f)
                verticalLineToRelative(80f)
                horizontalLineTo(120f)
                verticalLineToRelative(-240f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(94f)
                quadToRelative(51f, -64f, 124.5f, -99f)
                reflectiveQuadTo(480f, 120f)
                quadToRelative(75f, 0f, 140.5f, 28.5f)
                reflectiveQuadToRelative(114f, 77f)
                reflectiveQuadToRelative(77f, 114f)
                reflectiveQuadTo(840f, 480f)
                reflectiveQuadToRelative(-28.5f, 140.5f)
                reflectiveQuadToRelative(-77f, 114f)
                reflectiveQuadToRelative(-114f, 77f)
                reflectiveQuadTo(480f, 840f)
                moveToRelative(112f, -192f)
                lineTo(440f, 496f)
                verticalLineToRelative(-216f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(184f)
                lineToRelative(128f, 128f)
                close()
            }
        }.build()

        return _History!!
    }

private var _History: ImageVector? = null
