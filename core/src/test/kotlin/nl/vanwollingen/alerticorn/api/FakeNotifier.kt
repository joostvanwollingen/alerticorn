package nl.vanwollingen.alerticorn.api

class FakeNotifier : Notifier {

    override fun getPlatform() = "fake"
    val calls: MutableMap<String, AlerticornMessage> = mutableMapOf()

    override fun send(message: AlerticornMessage, destination: String) {
        calls[destination] = message
    }

    fun clear() = calls.clear()
}