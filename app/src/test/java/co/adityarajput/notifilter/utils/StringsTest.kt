package co.adityarajput.notifilter.utils

import kotlin.test.Test
import kotlin.test.assertContains

class StringsTest {
    @Test
    fun formatBundleMap_safeForMixedValues() {
        val string = formatBundleMap(
            mapOf(
                "null" to null,
                "boolean" to true,
                "integer" to 42,
                "float" to 6.9,
                "string" to "string",
                "object" to object {
                    @Suppress("unused")
                    private val param: Int = 42
                },
            ),
        )

        assertContains(string, "null=null")
        assertContains(string, "boolean=true")
        assertContains(string, "integer=42")
        assertContains(string, "float=6.9")
        assertContains(string, "string=string")
        assertContains(string, "object=Object")
    }
}
