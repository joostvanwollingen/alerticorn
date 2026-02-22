package org.example

import io.kotest.core.config.AbstractProjectConfig
import nl.vanwollingen.alerticorn.api.Event
import nl.vanwollingen.alerticorn.kotest.AlerticornProjectExtension

// ---------------------------------------------------------------------------
// PROJECT-LEVEL NOTIFICATIONS
//
// Use AlerticornProjectExtension to send a notification when the entire
// test suite starts or finishes. The SUITE_COMPLETE event includes summary
// details: total, passed, failed, errors, and ignored counts.
//
// This class is auto-discovered by Kotest via the classpath.
// ---------------------------------------------------------------------------
class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(
        AlerticornProjectExtension(
            title = "Kotest suite finished",
            body = "See details below for test result summary.",
            platform = "teams",
            channel = MY_TEAMS_WEBHOOK,
            events = listOf(Event.SUITE_COMPLETE),
        )
    )
}
