package co.adityarajput.notifilter.data

import co.adityarajput.notifilter.data.models.Action
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {
    @Test
    fun Converters_Action_roundtrip() {
        Converters().run {
            Action.entries.forEach {
                assertTrue(it == toAction(fromAction(it)))
            }
        }
    }

    @Test
    fun Converters_Days_roundtrip() {
        Converters().run {
            listOf(
                setOf(1, 2), setOf(2, 1),
                setOf(2, 4, 6), setOf(2, 3, 5),
                setOf(0, 1, 2, 3, 4, 5, 6),
            ).forEach {
                assertTrue(it == toDays(fromDays(it)))
            }
        }
    }
}
