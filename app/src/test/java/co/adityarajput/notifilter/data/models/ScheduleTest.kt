package co.adityarajput.notifilter.data.models

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScheduleTest {
    private val mondayEightAM = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 0)
    }

    private val mondayNineAM = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
    }

    private val mondayTenAM: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
    }

    private val mondayFivePM = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 17)
        set(Calendar.MINUTE, 0)
    }

    private val tuesdayEightAM = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 0)
    }

    private val tuesdayNinePM: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        set(Calendar.HOUR_OF_DAY, 21)
        set(Calendar.MINUTE, 0)
    }

    private val saturdayEightAm = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 0)
    }

    private val saturdayNineAm = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        set(Calendar.HOUR_OF_DAY, 9)
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
        assertTrue(atHome.includesNow(mondayEightAM))
        assertTrue(atWork.includesNow(mondayNineAM))
        assertTrue(atWork.includesNow(mondayTenAM))
        assertTrue(atWork.includesNow(mondayFivePM))

        assertTrue(atHome.includesNow(mondayNineAM))
        assertTrue(atHome.includesNow(mondayFivePM))
        assertTrue(atHome.includesNow(tuesdayEightAM))
        assertTrue(atHome.includesNow(tuesdayNinePM))

        assertTrue(weekend.includesNow(saturdayEightAm))
        assertTrue(weekend.includesNow(saturdayNineAm))
        assertTrue(weekend.includesNow(saturdayElevenPM))
    }

    @Test
    fun Schedule_includesNow_incorrectDay() {
        assertFalse(atWork.includesNow(saturdayEightAm))
        assertFalse(atWork.includesNow(saturdayNineAm))
        assertFalse(atWork.includesNow(saturdayElevenPM))

        assertFalse(atHome.includesNow(saturdayEightAm))
        assertFalse(atHome.includesNow(saturdayNineAm))
        assertFalse(atHome.includesNow(saturdayElevenPM))

        assertFalse(weekend.includesNow(mondayEightAM))
        assertFalse(weekend.includesNow(mondayNineAM))
        assertFalse(weekend.includesNow(mondayTenAM))
        assertFalse(weekend.includesNow(mondayFivePM))
        assertFalse(weekend.includesNow(tuesdayEightAM))
        assertFalse(weekend.includesNow(tuesdayNinePM))
    }

    @Test
    fun Schedule_includesNow_incorrectTime() {
        assertFalse(atWork.includesNow(mondayEightAM))
        assertFalse(atWork.includesNow(tuesdayEightAM))
        assertFalse(atWork.includesNow(tuesdayNinePM))

        assertFalse(atHome.includesNow(mondayTenAM))
        assertFalse(atHome.includesNow(saturdayEightAm))
    }

    @Test
    fun Schedule_isRangeValid() {
        assertTrue(Schedule(0, 1439).isRangeValid())
        assertTrue(Schedule(60, 120).isRangeValid())
        assertTrue(Schedule(120, 60).isRangeValid())

        assertFalse(Schedule(-1, 1439).isRangeValid())
        assertFalse(Schedule(0, 1440).isRangeValid())
        assertFalse(Schedule(60, 60).isRangeValid())
    }
}
