package nl.vanwollingen.alerticorn.api

data class AlerticornMessage @JvmOverloads constructor(
    val title: String,
    val body: String? = null,
    val details: Map<String, String>? = emptyMap(),
    val links: Map<String, String>? = emptyMap(),
    val throwable: Throwable? = null,
)