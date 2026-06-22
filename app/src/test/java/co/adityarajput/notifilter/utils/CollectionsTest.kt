package co.adityarajput.notifilter.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionsTest {
    val numbers = (0..9).toList()

    val isEven = { it: Int -> it % 2 == 0 }

    @Test
    fun Collections_filterFirst_returnsEarly() {
        assertEquals(emptyList<Int>() to false, numbers.filterFirst(0, isEven))
        assertEquals(listOf(0) to false, numbers.filterFirst(1, isEven))
        assertEquals(listOf(0, 2, 4) to false, numbers.filterFirst(3, isEven))
    }

    val isLargerThanTen = { it: Int -> it > 10 }

    @Test
    fun Collections_filterFirst_returnsComplete() {
        assertEquals(listOf(0, 2, 4, 6, 8) to true, numbers.filterFirst(10, isEven))
        assertEquals(emptyList<Int>() to true, numbers.filterFirst(10, isLargerThanTen))
    }
}
