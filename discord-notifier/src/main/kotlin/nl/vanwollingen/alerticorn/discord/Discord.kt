package nl.vanwollingen.alerticorn.discord

import nl.vanwollingen.alerticorn.api.Message

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Message.Platform("discord")
annotation class Discord