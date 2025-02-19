package nl.vanwollingen.alerticorn.teams

import nl.vanwollingen.alerticorn.api.Message

/**
 * Shorthand for [Message.Platform], to mark a function or class as targeting the "teams" platform.
 **
 * @see Message.Platform for information on platform annotations.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Message.Platform("teams")
annotation class Teams
