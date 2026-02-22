import nl.vanwollingen.alerticorn.api.Event;
import nl.vanwollingen.alerticorn.api.Message;
import nl.vanwollingen.alerticorn.slack.Slack;
import nl.vanwollingen.alerticorn.testng.MessageListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

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

// Register the Alerticorn TestNG listener.
// Alternatives: SPI auto-discovery (just add the jar) or testng.xml configuration.
@Listeners(MessageListener.class)
// Suite-level notification: sends a summary when the entire suite finishes.
@Message(title = "Test suite completed")
@Message.Events({Event.SUITE_COMPLETE})
@Slack
public class ExampleTest {

    private static final String MY_SLACK_WEBHOOK = "https://hooks.slack.com/services/YOUR/WEBHOOK/URL";

    // -----------------------------------------------------------------------
    // Basic usage: annotate a test with @Slack and @Message.Channel to send
    // a notification to Slack when the test fails.
    // -----------------------------------------------------------------------
    @Test
    @Slack
    @Message(title = "Login test failed")
    @Message.Channel(MY_SLACK_WEBHOOK)
    public void basic_failure_notification() {
        assertEquals(true, false);
    }

    // -----------------------------------------------------------------------
    // Use @Message.Events to only notify on specific outcomes.
    // This test only sends a notification on FAIL.
    // -----------------------------------------------------------------------
    @Test
    @Slack
    @Message(title = "Search functionality broken")
    @Message.Events({Event.FAIL})
    @Message.Channel(MY_SLACK_WEBHOOK)
    public void notify_only_on_failure() {
        assertEquals(true, false);
    }

    // -----------------------------------------------------------------------
    // A passing test -- no notification is sent by default because the
    // class-level @Message.Events only includes SUITE_COMPLETE.
    // The method-level annotations below are not present, so no method-level
    // notification triggers either.
    // -----------------------------------------------------------------------
    @Test
    public void a_passing_test() {
        assertEquals(true, true);
    }
}
