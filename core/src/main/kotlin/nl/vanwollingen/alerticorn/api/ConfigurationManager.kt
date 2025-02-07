package nl.vanwollingen.alerticorn.api

/**
 * A utility object to load environment-based Alerticorn configuration values.
 *
 * This object provides functionality to load, reload, and retrieve configuration values
 * stored in the environment variables. It filters for keys that start with the prefix `AC_`
 * and provides convenience methods to access them, such as by platform,
 * key, or channel.
 *
 * @constructor This class is a singleton object that does not require instantiation.
 */
object ConfigurationManager {
    private const val AC_ENV_PREFIX = "AC_"
    private var environment = loadFromEnvironment()

    @JvmStatic
    private fun loadFromEnvironment() = System.getenv().mapNotNull {
            if (it.key.startsWith(AC_ENV_PREFIX)) it.key.removePrefix(AC_ENV_PREFIX).uppercase() to it.value
            else null
        }.toMap()

    /**
     * Reloads the environment variables and updates the internal [environment] map.
     * This should be used if the environment variables are modified, and you need to refresh the configuration.
     */
    @JvmStatic
    fun reload() {
        environment = loadFromEnvironment()
    }

    /**
     * Retrieves the configuration value for a specific platform and key from the environment variables.
     *
     * The platform and key are both converted to uppercase before being used to search the environment map.
     *
     * @param platform The platform identifier (e.g., "discord", "slack").
     * @param key The configuration key (e.g., "database_url", "api_key").
     * @return The corresponding value from the environment, or `null` if not found.
     */
    @JvmStatic
    fun get(platform: String, key: String): String? = environment["${platform.uppercase()}_${key.uppercase()}"]

    /**
     * Retrieves the configuration value for a given key from the environment variables.
     *
     * The key is converted to uppercase before being used to search the environment map.
     *
     * @param key The configuration key (e.g., "api_key", "secret_token").
     * @return The corresponding value from the environment, or `null` if not found.
     */
    @JvmStatic
    fun get(key: String): String? = environment[key.uppercase()]

    /**
     * Retrieves the channel-specific configuration value for a given platform and channel name.
     *
     * The platform and channel name are both converted to uppercase before being used to search the environment map.
     *
     * @param platform The platform identifier (e.g., "slack", "teams").
     * @param name The channel name (e.g., "general", "alerts").
     * @return The corresponding channel configuration value from the environment, or `null` if not found.
     */
    @JvmStatic
    fun channel(platform: String, name: String): String? = environment["${platform.uppercase()}_CHANNEL_${name.uppercase()}"]
}