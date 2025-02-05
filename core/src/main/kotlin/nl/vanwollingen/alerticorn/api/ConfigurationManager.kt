package nl.vanwollingen.alerticorn.api

object ConfigurationManager {
    private const val AC_ENV_PREFIX = "AC_"
    private var environment = loadFromEnvironment() //TODO could have field getter?

    private fun loadFromEnvironment() = System.getenv().mapNotNull {
            if (it.key.startsWith(AC_ENV_PREFIX)) it.key.removePrefix(AC_ENV_PREFIX).uppercase() to it.value
            else null
        }.toMap()

    fun reload() {
        environment = loadFromEnvironment()
    }

    fun get(platform: String, key: String) = environment["${platform.uppercase()}_${key.uppercase()}"]
    fun get(key: String) = environment[key.uppercase()]
    fun channel(platform: String, name: String) = environment["${platform.uppercase()}_CHANNEL_${name.uppercase()}"]
}