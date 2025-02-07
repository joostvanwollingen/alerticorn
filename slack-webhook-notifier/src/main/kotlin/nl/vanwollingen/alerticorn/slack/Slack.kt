package nl.vanwollingen.alerticorn.slack

import nl.vanwollingen.alerticorn.api.Message

/**
 * Shorthand for [Message.Platform], to marks a function or class as targeting the "slack" platform.
 **
 * @see Message.Platform for information on platform annotations.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Message.Platform("slack")
annotation class Slack
