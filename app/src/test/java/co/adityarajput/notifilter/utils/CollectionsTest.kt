package co.adityarajput.notifilter.utils

import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionsTest {
    val numbers = (0..9).toList()

    val isEven = { it: Int -> it % 2 == 0 }

    @Test
    fun Collections_filterFirst_returnsEarly() {
        assertTrue(numbers.filterFirst(0, isEven) == emptyList<Int>() to false)
        assertTrue(numbers.filterFirst(1, isEven) == listOf(0) to false)
        assertTrue(numbers.filterFirst(3, isEven) == listOf(0, 2, 4) to false)
    }

    val isLargerThanTen = { it: Int -> it > 10 }

    @Test
    fun Collections_filterFirst_returnsComplete() {
        assertTrue(numbers.filterFirst(10, isEven) == listOf(0, 2, 4, 6, 8) to true)
        assertTrue(numbers.filterFirst(10, isLargerThanTen) == emptyList<Int>() to true)
    }
}
