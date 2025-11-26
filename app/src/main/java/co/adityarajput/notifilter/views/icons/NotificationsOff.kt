package co.adityarajput.notifilter.views.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val NotificationsOff: ImageVector
    get() {
        if (_NotificationsOff != null) return _NotificationsOff!!

        _NotificationsOff = ImageVector.Builder(
            name = "Notifications_off",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(160f, 760f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(-280f)
                quadToRelative(0f, -33f, 8.5f, -65f)
                reflectiveQuadToRelative(25.5f, -61f)
                lineToRelative(60f, 60f)
                quadToRelative(-7f, 16f, -10.5f, 32.5f)
                reflectiveQuadTo(320f, 400f)
                verticalLineToRelative(280f)
                horizontalLineToRelative(248f)
                lineTo(56f, 168f)
                lineToRelative(56f, -56f)
                lineToRelative(736f, 736f)
                lineToRelative(-56f, 56f)
                lineToRelative(-146f, -144f)
                close()
                moveToRelative(560f, -154f)
                lineToRelative(-80f, -80f)
                verticalLineToRelative(-126f)
                quadToRelative(0f, -66f, -47f, -113f)
                reflectiveQuadToRelative(-113f, -47f)
                quadToRelative(-26f, 0f, -50f, 8f)
                reflectiveQuadToRelative(-44f, 24f)
                lineToRelative(-58f, -58f)
                quadToRelative(20f, -16f, 43f, -28f)
                reflectiveQuadToRelative(49f, -18f)
                verticalLineToRelative(-28f)
                quadToRelative(0f, -25f, 17.5f, -42.5f)
                reflectiveQuadTo(480f, 80f)
                reflectiveQuadToRelative(42.5f, 17.5f)
                reflectiveQuadTo(540f, 140f)
                verticalLineToRelative(28f)
                quadToRelative(80f, 20f, 130f, 84.5f)
                reflectiveQuadTo(720f, 400f)
                close()
                moveTo(480f, 880f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(400f, 800f)
                horizontalLineToRelative(160f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(480f, 880f)
                moveToRelative(33f, -481f)
            }
        }.build()

        return _NotificationsOff!!
    }

private var _NotificationsOff: ImageVector? = null
