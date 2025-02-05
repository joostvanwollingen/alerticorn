package nl.vanwollingen.alerticorn.api

data class AlerticornMessage(
    val title: String,
    val body: String? = null,
    val details: Map<String, String>? = emptyMap(),
    val links: Map<String, String>? = emptyMap(),
    val throwable: Throwable? = null,
)