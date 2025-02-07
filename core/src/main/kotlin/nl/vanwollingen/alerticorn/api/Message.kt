package nl.vanwollingen.alerticorn.api

/**
 * Annotation for defining message content.

 * This annotation can be applied to functions or classes to specify message details such as
 * title, body, additional details, and related links.
 *
 * @property title The title of the message.
 * @property body The optional body content of the message.
 * @property details Additional details associated with the message.
 * @property links Related links relevant to the message.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Message(
    val title: String,
    val body: String = "",
    val details: Array<String> = [],
    val links: Array<String> = [],
) {

    /**
     * This annotation can be used to filter messages that are triggered based on specific events,
     * such as test failures, passes, or exceptions. Multiple events can be specified, [Event.ANY] will filter nothing.
     *
     * @see Event
     * @property value An array of event types for which to send this message.
     */
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    annotation class Events(
        val value: Array<Event> = [],
    )

    /**
     * Annotation for specifying the target channel for a message.
     *
     * @property value The name of the channel where the message should be sent.
     */
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    annotation class Channel(
        val value: String,
    )

    /**
     * Annotation for specifying a message template.
     *
     * This annotation can be used to reference a predefined template format for messages previously
     * stored using [MessageProvider.store].
     *
     * @property value The template identifier.
     * @see MessageProvider
     */
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    annotation class Template(
        val value: String,
    )

    /**
     * Annotation for specifying the platform where the message should be delivered.
     *
     * This annotation is useful when using multiple platforms in the same project.
     *
     * @property value The name of the platform.
     */
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    annotation class Platform(
        val value: String,
    )
}

/**
 * Enum representing different types of events that can trigger a message.
 */
enum class Event {
    /** Represents any event type. */
    ANY,

    /** Represents an exception occurrence, including test failure */
    EXCEPTION,

    /** Represents a test or process failure. */
    FAIL,

    /** Represents a successful test or process completion. */
    PASS,

    /** Represents a skipped test or process. */
    SKIP,

    /** Represents an aborted test execution. */
    ABORTED,

    /** Represents a disabled test. */
    DISABLED,

//    SUITE_COMPLETE, // TODO: Align with test framework naming (BEFORE_ALL/AFTER_ALL)
//    SUITE_START,
}