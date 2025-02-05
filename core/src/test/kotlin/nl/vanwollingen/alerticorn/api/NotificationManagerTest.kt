package nl.vanwollingen.alerticorn.api

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows

class NotificationManagerTest {

    @BeforeEach
    fun beforeEach() {
        (NotificationManager.getNotifier("fake")!! as FakeNotifier).clear()
    }

    @Test
    fun `given a default platform and channel, when notifying by message, the default notifier is called`() {
        val theMessage = AlerticornMessage("test", "test")
        NotificationManager.setDefaultPlatform("fake")
        NotificationManager.setDefaultChannel("test")

        NotificationManager.notify(theMessage)

        val notifier = NotificationManager.getNotifier("fake")!! as FakeNotifier
        assertEquals(theMessage, notifier.calls["test"])
    }

    @Test
    fun `given a testnotifier platform, when setting the default platform, the default platform is returned`() {
        NotificationManager.setDefaultPlatform("fake")
        assertEquals("fake", NotificationManager.getDefaultPlatform())
    }

    @Test
    fun `given a testnotifier platform, when getting default platform, the first platform is returned`() {
        assertEquals("fake", NotificationManager.getDefaultPlatform())
    }

    @Test
    fun `when setting a default platform that doesn't exist, an exception is thrown`() {
        val exception = assertThrows<Error> { NotificationManager.setDefaultPlatform("myWrongPlatform") }
        assertEquals("No notifier available for platform: myWrongPlatform", exception.message)
    }

    @Test
    fun `when notifying by platform, channel and message, the platform notifier is called`() {
        val theMessage = AlerticornMessage("test", "test")

        NotificationManager.notify("fake", theMessage, "somewere")

        val notifier = NotificationManager.getNotifier("fake")!! as FakeNotifier
        assertEquals(theMessage, notifier.calls["somewere"])
    }

    @Test
    fun `when notifying by platform that doesn't exists, a MessageFailedToSend Exception is thrown`() {
        val theMessage = AlerticornMessage("test", "test")

        assertThrows<MessageFailedToSendException>() {
            NotificationManager.notify("pasbien", theMessage, "somewere")
        }

        assertEquals(null, NotificationManager.getNotifier("pasbien"))
    }

    @Test
    fun `given a default platform, when notifying by message and channel, the default notifier is called`() {
        val theMessage = AlerticornMessage("test", "test")
        NotificationManager.setDefaultPlatform("fake")

        NotificationManager.notify(theMessage, "somewere")

        val notifier = NotificationManager.getNotifier("fake")!! as FakeNotifier

        assertEquals(theMessage, notifier.calls["somewere"])
    }
}