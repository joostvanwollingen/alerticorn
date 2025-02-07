package nl.vanwollingen.alerticorn.api

import java.util.ServiceLoader
import java.util.concurrent.atomic.AtomicReference

/**
 * Manages notification delivery using available [Notifier]s.
 *
 * This singleton loads notifiers dynamically and provides methods to send messages
 * across different platforms.
 *
 */
object NotificationManager {
    private val config = ConfigurationManager

    private val defaultPlatform = AtomicReference<String?>(config.get("DEFAULT_PLATFORM"))
    private val defaultChannel = AtomicReference<String?>(config.get("DEFAULT_CHANNEL"))

    private val notifiers: Map<String, Notifier> by lazy {
        ServiceLoader.load(Notifier::class.java).associateBy { it.getPlatform().lowercase() }
    }

    /**
     * Sends a notification using the specified platform and destination.
     *
     * @param platform The platform to send the notification (e.g., Slack, Discord).
     * @param message The message to be sent.
     * @param destination The destination (e.g., channel).
     * @throws MessageFailedToSendException if the platform or destination is missing.
     */
    @JvmStatic
    fun notify(
        platform: String? = getDefaultPlatform(),
        message: AlerticornMessage,
        destination: String? = defaultChannel.get(),
    ) {
        if (destination == null) throw MessageFailedToSendException("Unable to send message. Missing destination. Set AC_DEFAULT_CHANNEL or provide a destination")
        if (platform == null) throw MessageFailedToSendException("Unable to determine platform. Set environment variable AC_DEFAULT_PLATFORM or make sure there's a notifier on the classpath.")
        if (notifiers[platform.lowercase()] == null) throw MessageFailedToSendException("Notifier for $platform is not available. Make sure the notifier for $platform is on the classpath.")

        val destinationChannel = config.channel(platform, destination) ?: destination
        notifiers[platform.lowercase()]?.send(message, destinationChannel)
    }

    /**
     * Sends a notification using the default platform and a specified destination.
     *
     * @param message The message to be sent.
     * @param destination The destination for the message.
     */
    @JvmStatic
    fun notify(message: AlerticornMessage, destination: String) =
        notifiers[defaultPlatform.get()]?.send(message, destination)

    /**
     * Sends a notification using the default platform and default destination.
     *
     * @param message The message to be sent.
     */
    @JvmStatic
    fun notify(message: AlerticornMessage) = defaultChannel.get()?.let {
        notifiers[defaultPlatform.get()]?.send(message, it)
    }

    /**
     * Retrieves the default notification platform.
     *
     * @return The default platform name, or the first available platform.
     */
    @JvmStatic
    fun getDefaultPlatform(): String? = defaultPlatform.get() ?: notifiers.keys.firstOrNull()

    /**
     * Sets the default notification platform.
     *
     * @param name The name of the platform to be set as default.
     * @throws Error if the specified platform is not available.
     */
    @JvmStatic
    fun setDefaultPlatform(name: String) =
        notifiers[name]?.run { defaultPlatform.set(name) } ?: throw Error("No notifier available for platform: $name")

    /**
     * Sets the default notification channel.
     *
     * @param name The name of the channel to be set as default.
     */
    @JvmStatic
    fun setDefaultChannel(name: String) = defaultChannel.set(name)

    /**
     * Retrieves the notifier for a given platform.
     *
     * @param name The platform name.
     * @return The corresponding notifier instance, or null if not found.
     */
    @JvmStatic
    fun getNotifier(name: String) = notifiers[name]
}