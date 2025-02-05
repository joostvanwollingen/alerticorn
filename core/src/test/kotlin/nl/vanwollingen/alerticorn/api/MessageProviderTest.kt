package nl.vanwollingen.alerticorn.api

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MessageProviderTest {
    @Test
    fun `it should store a message function and return it's result when called`() {
        val funKey = "myKey"
        val throwable = Throwable("oops")
        val details = mapOf("1" to "2")
        val links = mapOf("3" to "4")
        val function: (String, Throwable?)->AlerticornMessage = { m, t -> AlerticornMessage(m, "${t?.message}", details, links, t) }

        MessageProvider.store(funKey, function)

        val message = "hoi"
        val result = MessageProvider.run(funKey, message, throwable)

        assertEquals(message, result.title)
        assertEquals(throwable.message, result.body)
        assertEquals(details, result.details)
        assertEquals(links, result.links)
        assertSame(throwable, result.throwable)
    }

    @Test
    fun `it should store a message function and return it on get`() {
        MessageProvider.store("myKey") { m, t -> AlerticornMessage(m, "${t?.message}", mapOf(), mapOf(), t) }

        val message = MessageProvider.get("myKey")
        assertTrue { message is (String, Throwable?) -> AlerticornMessage }
    }

    @Test
    fun `it should throw when getting an unknown key`() {
        val error = assertThrows<Error> { MessageProvider.run("unknown key", "hi", null) }
        assertEquals(error.message, "Key unknown key not found.")
    }
}