package nl.vanwollingen.alerticorn.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junitpioneer.jupiter.SetEnvironmentVariable

// TODO: nasty order issue, due to using real, singleton NotificationManager for the tests
// This runs first, in order to have the default read from the environment
// Depends on junit.jupiter.testclass.order.default
@Order(1)
class NotificationManagerDefaultTest {
    @SetEnvironmentVariable(key = "AC_DEFAULT_PLATFORM", value = "myPlatform")
    @Test
    fun `given a configured default platform, when getting the default platform, the configured platform is returned`() {
        assertEquals("myPlatform", NotificationManager.getDefaultPlatform())
    }
}