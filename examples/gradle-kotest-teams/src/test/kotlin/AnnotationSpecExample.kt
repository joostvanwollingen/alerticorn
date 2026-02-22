package org.example

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import nl.vanwollingen.alerticorn.api.Event
import nl.vanwollingen.alerticorn.api.Message
import nl.vanwollingen.alerticorn.kotest.AlerticornExtension
import nl.vanwollingen.alerticorn.teams.Teams

// ---------------------------------------------------------------------------
// WEBHOOK CONFIGURATION
//
// Replace the URL below with your actual Teams webhook URL.
//
// IMPORTANT: Never commit real webhook URLs to version control.
//
// The recommended approach is to use environment variables:
//   export AC_TEAMS_CHANNEL_ALERTS=https://your-org.webhook.office.com/webhookb2/YOUR/WEBHOOK
//
// Then use @Message.Channel("alerts") and Alerticorn resolves the webhook via
// the AC_TEAMS_CHANNEL_ALERTS environment variable automatically.
//
// For local development you can use a .env file (make sure .env is in .gitignore).
// In CI, use your platform's secret management (GitHub Actions secrets, etc.).
// ---------------------------------------------------------------------------
const val MY_TEAMS_WEBHOOK = "https://your-org.webhook.office.com/webhookb2/YOUR/WEBHOOK"

// ---------------------------------------------------------------------------
// APPROACH A: Spec-level annotations
//
// Place @Message annotations on the Spec class. All tests in the spec
// inherit the same notification settings. This works best with AnnotationSpec
// where method-level annotations can override class-level ones.
// ---------------------------------------------------------------------------
@Message(title = "Payment spec failed")
@Message.Events([Event.FAIL])
@Teams
@Message.Channel(MY_TEAMS_WEBHOOK)
class AnnotationSpecExample : AnnotationSpec() {

    init {
        // Register the Alerticorn extension for this spec.
        extensions(AlerticornExtension())
    }

    // All tests inherit the class-level @Message configuration.
    @Test
    fun process_payment() {
        true shouldBe false
    }

    // Method-level annotations override class-level ones in AnnotationSpec.
    @Test
    @Message(title = "Refund test failed - urgent!")
    fun refund_payment() {
        true shouldBe false
    }
}
