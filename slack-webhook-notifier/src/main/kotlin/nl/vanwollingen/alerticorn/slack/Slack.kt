package nl.vanwollingen.alerticorn.slack

import nl.vanwollingen.alerticorn.api.Message

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Message.Platform("slack")
annotation class Slack
