package nl.vanwollingen.alerticorn.teams

import nl.vanwollingen.alerticorn.api.AlerticornMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TeamsWebHookMessageTest {

    @Test
    fun `it should convert message with attachments to JSON body`() {
        val message = AlerticornMessage(
            title = "And then, with a bang!, something terrible happened",
            body = "The tree came crashing down and hit the car in the middle of the a-pillar. Glass shattered, steel bent. It became very quiet. Only the hissing of the carburetor could be heard over the screams.",
            details = mapOf(
                "Team" to "Team Red",
                "App" to "Major Lazor",
            ),
            links = mapOf(
                "Runbook" to "https://www.google.com"
            ),
            throwable = Error("Something went wrong", Error("My face just exploded."))
        )
        val expectedJson =
            """{"type": "message","attachments": [{"contentType": "application/vnd.microsoft.card.adaptive","content": {"type": "AdaptiveCard","body": [{"type": "RichTextBlock","inlines": [{"type": "TextRun","text": "And then, with a bang!, something terrible happened","size": "medium","weight": "bolder"}]},{"type": "RichTextBlock","inlines": [{"type": "TextRun","text": "The tree came crashing down and hit the car in the middle of the a-pillar. Glass shattered, steel bent. It became very quiet. Only the hissing of the carburetor could be heard over the screams.","size": "medium"}]},{"type": "FactSet","facts": [{"title": "Exception","value": "Something went wrong"},{"title": "Cause","value": "java.lang.Error: My face just exploded."}]},{"type": "FactSet","facts": [{"title": "Team","value": "Team Red"},{"title": "App","value": "Major Lazor"}]},{"type": "FactSet","facts": [{"title": "[🔗](https://www.google.com)","value": "[Runbook](https://www.google.com)"}]}]}}]}"""

        assertEquals(expectedJson, message.toTeamsWebHookMessage().toJson())
    }
}