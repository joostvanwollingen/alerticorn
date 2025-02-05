package nl.vanwollingen.alerticorn.api

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junitpioneer.jupiter.SetEnvironmentVariable

class ConfigurationManagerTest {

    @BeforeEach
    fun beforeEach() {
        ConfigurationManager.reload()
    }

    @Test
    @SetEnvironmentVariable(key = "AC_SLACK_EMOJI", value = ":troll:")
    @SetEnvironmentVariable(key = "AC_SLACK_CHANNEL_DEFAULT", value = "#welcome")
    @SetEnvironmentVariable(key = "AC_SLACK_CHANNEL_GENERAL", value = "#general")
    @SetEnvironmentVariable(key = "AC_TEAMS_RETRY", value = "0")
    @SetEnvironmentVariable(key = "AC_TEAMS_CHANNEL_DEFAULT", value = "Everyone")
    @SetEnvironmentVariable(key = "AC_TEAMS_CHANNEL_GREEN", value = "Green")
    @SetEnvironmentVariable(key = "PRE_TEAMS_CHANNEL_GREEN", value = "Green")
    fun `it should remove prefix, get keys, channel and ignore case`() {
        ConfigurationManager.get("SLACK", "EMOJI") shouldBe  ":troll:"
        ConfigurationManager.get("SlACk", "emoji") shouldBe  ":troll:"
        ConfigurationManager.channel("SLACK", "DEFAULT") shouldBe  "#welcome"
        ConfigurationManager.channel("SLACK", "GENERAL") shouldBe  "#general"
        ConfigurationManager.get("TEAMS", "ReTrY") shouldBe  "0"
        ConfigurationManager.channel("TEAMS", "DEFAULT") shouldBe  "Everyone"
        ConfigurationManager.channel("TeAmS", "GREEN") shouldBe  "Green"
    }

    @Test
    @SetEnvironmentVariable(key = "TEAMS_CHANNEL_GREEN", value = "NOT_THERE")
    fun `it should only load AC_ prefixed variables`() {
        ConfigurationManager.channel("TEAMS", "GREEN") shouldBe null
    }

    @Test
    @SetEnvironmentVariable(key = "AC_EXAMPLE_SETTING", value = "1")
    fun `it should load keys directly`() {
        ConfigurationManager.get("EXAMPLE_SETTING") shouldBe "1"
    }
}