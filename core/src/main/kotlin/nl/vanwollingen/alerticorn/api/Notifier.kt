package nl.vanwollingen.alerticorn.api

interface Notifier {
    fun getPlatform(): String
    fun send(message: AlerticornMessage, destination: String)
}