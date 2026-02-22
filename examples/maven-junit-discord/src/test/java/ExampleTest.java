import nl.vanwollingen.alerticorn.api.AlerticornMessage;
import nl.vanwollingen.alerticorn.api.Event;
import nl.vanwollingen.alerticorn.api.Message;
import nl.vanwollingen.alerticorn.api.RunWith;
import nl.vanwollingen.alerticorn.discord.Discord;
import nl.vanwollingen.alerticorn.junit.MessageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

// ---------------------------------------------------------------------------
// WEBHOOK CONFIGURATION
//
// Replace the URL below with your actual Discord webhook URL.
//
// IMPORTANT: Never commit real webhook URLs to version control.
//
// The recommended approach is to use environment variables:
//   export AC_DISCORD_CHANNEL_ALERTS=https://discord.com/api/webhooks/YOUR/WEBHOOK
//
// Then use @Message.Channel("alerts") and Alerticorn resolves the webhook via
// the AC_DISCORD_CHANNEL_ALERTS environment variable automatically.
//
// For local development you can use a .env file (make sure .env is in .gitignore).
// In CI, use your platform's secret management (GitHub Actions secrets, etc.).
// ---------------------------------------------------------------------------

@ExtendWith(MessageExtension.class)
@Message(title = "A test in ExampleTest failed")
public class ExampleTest {

    private static final String MY_DISCORD_WEBHOOK = "https://discord.com/api/webhooks/YOUR/WEBHOOK";

    // -----------------------------------------------------------------------
    // Basic usage: annotate a test with @Discord and @Message.Channel to send
    // a notification to Discord when the test fails.
    // -----------------------------------------------------------------------
    @Test
    @Discord
    @Message.Channel(MY_DISCORD_WEBHOOK)
    public void basic_failure_notification() {
        assertEquals(true, false);
    }

    // -----------------------------------------------------------------------
    // Override the class-level @Message title on a specific test method.
    // -----------------------------------------------------------------------
    @Test
    @Discord
    @Message(title = "Order processing test failed!")
    @Message.Channel(MY_DISCORD_WEBHOOK)
    public void overridden_message_title() {
        assertEquals(true, false);
    }

    // -----------------------------------------------------------------------
    // Use @Message.Events to control WHEN a notification is sent.
    // This test only notifies on FAIL.
    // -----------------------------------------------------------------------
    @Test
    @Discord
    @Message(title = "Database migration test failed")
    @Message.Events({Event.FAIL})
    @Message.Channel(MY_DISCORD_WEBHOOK)
    public void notify_only_on_failure() {
        assertEquals(true, false);
    }

    // -----------------------------------------------------------------------
    // Programmatic API: use RunWith.message() from Java.
    // Useful when you need dynamic configuration or want to use Alerticorn
    // outside of a test framework.
    // -----------------------------------------------------------------------
    @Test
    public void programmatic_api_with_run_with() {
        RunWith.message(
                "discord",
                MY_DISCORD_WEBHOOK,
                "Health check completed",
                (title, result) -> new AlerticornMessage(
                        "Result: " + title,
                        "Service returned: " + result,
                        null,
                        null,
                        null
                ),
                () -> "all systems operational"
        );
    }
}
