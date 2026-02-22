package nl.vanwollingen.alerticorn.kotest

import nl.vanwollingen.alerticorn.api.Event
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AlerticornDslTest {

    @Test
    fun `alerticorn builder returns correctly configured extension`() {
        val ext = alerticorn(
            title = "Test alert",
            body = "Something failed",
            details = mapOf("env" to "prod"),
            links = mapOf("runbook" to "http://runbook.example.com"),
            events = listOf(Event.FAIL, Event.EXCEPTION),
            platform = "slack",
            channel = "alerts",
            template = null,
        )

        assertIs<AlerticornTestCaseExtension>(ext)
    }

    @Test
    fun `alerticorn builder with defaults returns extension`() {
        val ext = alerticorn(title = "Simple alert")
        assertIs<AlerticornTestCaseExtension>(ext)
    }

    @Test
    fun `alerticorn builder with template returns extension`() {
        val ext = alerticorn(
            title = "Template alert",
            template = "myTemplate",
            platform = "discord",
            channel = "dev",
        )
        assertIs<AlerticornTestCaseExtension>(ext)
    }
}
