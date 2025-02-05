package nl.vanwollingen.alerticorn.api

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Message(
    val title: String,
    val body: String = "",
    val details: Array<String> = [],
    val links: Array<String> = [],
) {
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    annotation class Events(
        val value: Array<Event> = [],
    )

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    annotation class Channel(
        val value: String,
    )

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    annotation class Template(
        val value: String,
    )

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    annotation class Platform(
        val value: String,
    )
}

enum class Event {
    ANY, EXCEPTION, FAIL, PASS, SKIP, ABORTED, DISABLED,
//    SUITE_COMPLETE, //TODO align with test framework name (BEFORE_ALL/AFTER_ALL),
//    SUITE_START,
}