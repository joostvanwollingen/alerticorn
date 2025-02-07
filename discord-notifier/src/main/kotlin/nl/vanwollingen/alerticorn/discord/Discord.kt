package nl.vanwollingen.alerticorn.discord

import nl.vanwollingen.alerticorn.api.Message

/**
 * Shorthand for [Message.Platform], to marks a function or class as targeting the "discord" platform.
 **
 * @see Message.Platform for information on platform annotations.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Message.Platform("discord")
annotation class Discord
