package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import nl.vanwollingen.alerticorn.api.Event
import nl.vanwollingen.alerticorn.kotest.alerticorn
import nl.vanwollingen.alerticorn.kotest.alerticornTest

// ---------------------------------------------------------------------------
// APPROACH B: Per-test DSL configuration
//
// Kotest's FunSpec, DescribeSpec, StringSpec etc. use lambdas for tests,
// so method-level annotations aren't possible. Instead, use the DSL:
//   - alerticornTest() convenience function (FunSpec only)
//   - alerticorn() builder with .config(extensions = ...)  (all spec styles)
// ---------------------------------------------------------------------------
class FunSpecDslExample : FunSpec({

    // -----------------------------------------------------------------------
    // alerticornTest() is a FunSpec convenience that combines test() with
    // the Alerticorn extension in a single call.
    // -----------------------------------------------------------------------
    alerticornTest(
        name = "checkout flow",
        title = "Checkout test failed!",
        platform = "teams",
        channel = MY_TEAMS_WEBHOOK,
        events = listOf(Event.FAIL),
    ) {
        true shouldBe false
    }

    // -----------------------------------------------------------------------
    // alerticorn() builder works with .config(extensions = ...) and is
    // compatible with ALL Kotest spec styles, not just FunSpec.
    // -----------------------------------------------------------------------
    test("inventory check").config(
        extensions = listOf(
            alerticorn(
                title = "Inventory check failed",
                body = "Could not verify stock levels",
                platform = "teams",
                channel = MY_TEAMS_WEBHOOK,
                events = listOf(Event.FAIL),
            )
        )
    ) {
        true shouldBe false
    }

    // -----------------------------------------------------------------------
    // alerticornTest() also works inside context blocks.
    // -----------------------------------------------------------------------
    context("user registration") {
        alerticornTest(
            name = "email validation",
            title = "Email validation broken",
            platform = "teams",
            channel = MY_TEAMS_WEBHOOK,
            events = listOf(Event.FAIL),
        ) {
            true shouldBe false
        }
    }
})
