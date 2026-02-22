package nl.vanwollingen.alerticorn.api

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StringArrayToMapTest {

    @Test
    fun `converts a balanced string array to a map`() {
        val result = stringArrayToMap(arrayOf("key1", "val1", "key2", "val2"))
        assertEquals(mapOf("key1" to "val1", "key2" to "val2"), result)
    }

    @Test
    fun `converts an unbalanced string array to a map with empty value for last key`() {
        val result = stringArrayToMap(arrayOf("key1", "val1", "orphan"))
        assertEquals(mapOf("key1" to "val1", "orphan" to ""), result)
    }

    @Test
    fun `converts an empty array to an empty map`() {
        val result = stringArrayToMap(emptyArray())
        assertEquals(emptyMap(), result)
    }

    @Test
    fun `converts a single-element array to a map with empty value`() {
        val result = stringArrayToMap(arrayOf("solo"))
        assertEquals(mapOf("solo" to ""), result)
    }
}
