package co.adityarajput.notifilter.data.models

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

@Suppress("TestFunctionName")
class ScheduleTest {
    private val mondayTenAM: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
    }

    private val tuesdayNinePM: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        set(Calendar.HOUR_OF_DAY, 21)
        set(Calendar.MINUTE, 0)
    }

    private val saturdayElevenPM: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 0)
    }

    private val atWork = Schedule(
        9 * 60,
        (12 + 5) * 60,
        setOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
        ),
    )

    private val atHome = Schedule(
        (12 + 5) * 60,
        9 * 60,
        setOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
        ),
    )

    private val weekend = Schedule(days = setOf(Calendar.SATURDAY, Calendar.SUNDAY))

    @Test
    fun Schedule_includesNow_correct() {
        assertTrue(atWork.includesNow(mondayTenAM))
        assertTrue(atHome.includesNow(tuesdayNinePM))
        assertTrue(weekend.includesNow(saturdayElevenPM))
    }

    @Test
    fun Schedule_includesNow_incorrectDay() {
        assertFalse(atWork.includesNow(saturdayElevenPM))
        assertFalse(atHome.includesNow(saturdayElevenPM))
        assertFalse(weekend.includesNow(mondayTenAM))
        assertFalse(weekend.includesNow(tuesdayNinePM))
    }

    @Test
    fun Schedule_includesNow_incorrectTime() {
        assertFalse(atWork.includesNow(tuesdayNinePM))
        assertFalse(atHome.includesNow(mondayTenAM))
    }
}
