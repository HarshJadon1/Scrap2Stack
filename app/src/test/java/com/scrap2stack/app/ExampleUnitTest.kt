package com.scrap2stack.app

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testMatchingLogicPlaceholder() {
        // Placeholder for future matching engine logic tests
        val developerSkills = listOf("Kotlin", "Go")
        val requiredSkills = listOf("Kotlin", "Android")
        
        val matched = developerSkills.intersect(requiredSkills)
        assertEquals(1, matched.size)
        assertTrue(matched.contains("Kotlin"))
    }
}
