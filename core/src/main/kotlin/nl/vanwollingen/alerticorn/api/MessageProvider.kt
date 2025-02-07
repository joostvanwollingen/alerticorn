package nl.vanwollingen.alerticorn.api

import java.util.concurrent.ConcurrentHashMap

/**
 * Stores and retrieves predefined message builders for different use cases.
 *
 * This singleton allows functions to be stored under keys and invoked later with a title
 * and an optional throwable.
 */
object MessageProvider {

    private val messages: MutableMap<String, (title: String, throwable: Throwable?) -> AlerticornMessage> =
        ConcurrentHashMap()

    /**
     * Stores a message-building function under a specified key.
     *
     * @param key The key to associate with the function.
     * @param function The function that generates an [AlerticornMessage].
     */
    @JvmStatic
    fun store(key: String, function: (title: String, throwable: Throwable?) -> AlerticornMessage) =
        messages.put(key, function)

    /**
     * Retrieves a stored message-building function by key.
     *
     * @param key The key of the stored function.
     * @return The corresponding function, or null if not found.
     */
    @JvmStatic
    fun get(key: String): ((String, Throwable?) -> AlerticornMessage)? = messages[key]

    /**
     * Executes a stored message-building function with the given parameters.
     *
     * @param key The key of the stored function.
     * @param title The title to pass to the function.
     * @param throwable An optional throwable to include in the message.
     * @return The generated [AlerticornMessage].
     * @throws Error if the key is not found.
     */
    @JvmStatic
    fun run(key: String, title: String, throwable: Throwable?): AlerticornMessage =
        get(key)?.invoke(title, throwable) ?: throw Error("Key $key not found.")
}
