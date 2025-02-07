package nl.vanwollingen.alerticorn.api

/**
 * Represents a message in the Alerticorn system, containing a title, optional body,
 * additional details, related links, and an optional throwable.
 *
 * This class is a Kotlin data class with default values for optional parameters.
 * It is annotated with [JvmOverloads] to allow Java-friendly constructors.
 *
 */
data class AlerticornMessage @JvmOverloads constructor(
    /** The title of the message. */
    val title: String,

    /** The optional body of the message. */
    val body: String? = null,

    /** Additional details related to the message as key-value pairs. */
    val details: Map<String, String>? = emptyMap(),

    /** Related links associated with the message as key-value pairs. */
    val links: Map<String, String>? = emptyMap(),

    /** An optional throwable associated with the message. */
    val throwable: Throwable? = null,
)
