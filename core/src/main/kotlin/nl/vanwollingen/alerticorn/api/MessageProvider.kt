package nl.vanwollingen.alerticorn.api

import java.util.concurrent.ConcurrentHashMap

object MessageProvider {

    private val messages: MutableMap<String, (title: String, throwable: Throwable?) -> AlerticornMessage> =
        ConcurrentHashMap()

    @JvmStatic
    fun store(key: String, function: (title: String, throwable: Throwable?) -> AlerticornMessage) =
        messages.put(key, function)

    @JvmStatic
    fun get(key: String): ((String, Throwable?) -> AlerticornMessage)? = messages[key]

    @JvmStatic
    fun run(key: String, title: String, throwable: Throwable?): AlerticornMessage =
        get(key)?.invoke(title, throwable) ?: throw Error("Key $key not found.")
}