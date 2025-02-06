package nl.vanwollingen.alerticorn.api

import java.util.ServiceLoader
import java.util.concurrent.atomic.AtomicReference

object NotificationManager {
    private val config = ConfigurationManager

    private val defaultPlatform = AtomicReference<String?>(config.get("DEFAULT_PLATFORM"))
    private val defaultChannel = AtomicReference<String?>(config.get("DEFAULT_CHANNEL"))

    private val notifiers: Map<String, Notifier> by lazy {
        ServiceLoader.load(Notifier::class.java).associateBy { it.getPlatform().lowercase() }
    }

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

    @JvmStatic
    fun notify(message: AlerticornMessage, destination: String) =
        notifiers[defaultPlatform.get()]?.send(message, destination)

    @JvmStatic
    fun notify(message: AlerticornMessage) = defaultChannel.get()?.let {
        notifiers[defaultPlatform.get()]?.send(message, it)
    }

    @JvmStatic
    fun getDefaultPlatform(): String? = defaultPlatform.get() ?: notifiers.keys.firstOrNull()

    @JvmStatic
    fun setDefaultPlatform(name: String) =
        notifiers[name]?.run { defaultPlatform.set(name) } ?: throw Error("No notifier available for platform: $name")

    @JvmStatic
    fun setDefaultChannel(name: String) = defaultChannel.set(name)

    @JvmStatic
    fun getNotifier(name: String) = notifiers[name]
}