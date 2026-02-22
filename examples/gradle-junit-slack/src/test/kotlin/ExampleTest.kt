package org.example

import nl.vanwollingen.alerticorn.api.*
import nl.vanwollingen.alerticorn.junit.MessageExtension
import nl.vanwollingen.alerticorn.slack.Slack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

// ---------------------------------------------------------------------------
// WEBHOOK CONFIGURATION
//
// Replace the URL below with your actual Slack webhook URL.
//
// IMPORTANT: Never commit real webhook URLs to version control.
//
// The recommended approach is to use environment variables:
//   export AC_SLACK_CHANNEL_ALERTS=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
//
// Then use @Message.Channel("alerts") and Alerticorn resolves the webhook via
// the AC_SLACK_CHANNEL_ALERTS environment variable automatically.
//
// For local development you can use a .env file (make sure .env is in .gitignore).
// In CI, use your platform's secret management (GitHub Actions secrets, etc.).
// ---------------------------------------------------------------------------
const val MY_SLACK_WEBHOOK = "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"

@ExtendWith(MessageExtension::class)
@Message(title = "A test in ExampleTest failed")
class ExampleTest {

    // -----------------------------------------------------------------------
    // Basic usage: annotate a test with @Slack and @Message.Channel to send
    // a notification to Slack when the test fails.
    // -----------------------------------------------------------------------
    @Test
    @Slack
    @Message.Channel(MY_SLACK_WEBHOOK)
    fun basic_failure_notification() {
        assertEquals(true, false)
    }

    // -----------------------------------------------------------------------
    // Override the class-level @Message title on a specific test method.
    // -----------------------------------------------------------------------
    @Test
    @Slack
    @Message(title = "Payment processing test failed!")
    @Message.Channel(MY_SLACK_WEBHOOK)
    fun overridden_message_title() {
        assertEquals(true, false)
    }

    // -----------------------------------------------------------------------
    // Use @Message.Events to control WHEN a notification is sent.
    // This test only sends a notification on FAIL, not on PASS or other events.
    // -----------------------------------------------------------------------
    @Test
    @Slack
    @Message(title = "Critical checkout test failed")
    @Message.Events([Event.FAIL])
    @Message.Channel(MY_SLACK_WEBHOOK)
    fun notify_only_on_failure() {
        assertEquals(true, false)
    }

    // -----------------------------------------------------------------------
    // Use a MessageProvider template to build a custom message.
    // Templates are registered in the companion object below.
    // -----------------------------------------------------------------------
    @Test
    @Slack
    @Message(title = "Inventory sync failed")
    @Message.Template("detailedReport")
    @Message.Channel(MY_SLACK_WEBHOOK)
    fun using_a_message_template() {
        throw RuntimeException("Connection to inventory service timed out")
    }

    // -----------------------------------------------------------------------
    // Programmatic API: use RunWith.messageOnException() instead of
    // annotations. This is useful for non-test code or when you need
    // dynamic configuration.
    // -----------------------------------------------------------------------
    @Test
    fun programmatic_api_with_run_with() {
        assertThrows<RuntimeException> {
            RunWith.messageOnException(
                platform = "slack",
                destination = MY_SLACK_WEBHOOK,
                title = "Background job failed",
                messageBuilder = { title, error ->
                    AlerticornMessage(
                        title = title,
                        body = "Error: $error",
                        details = mapOf("component" to "scheduler", "severity" to "high"),
                    )
                },
                block = {
                    throw RuntimeException("Cron job timed out")
                }
            )
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun registerTemplates() {
            // Register a reusable message template that includes structured details and links.
            MessageProvider.store("detailedReport") { title, throwable ->
                AlerticornMessage(
                    title = title,
                    body = "Error: ${throwable?.message}",
                    details = mapOf("template" to "detailedReport", "environment" to "CI"),
                    links = mapOf("runbook" to "https://wiki.example.com/runbooks/inventory-sync"),
                    throwable = throwable,
                )
            }
        }
    }
}
