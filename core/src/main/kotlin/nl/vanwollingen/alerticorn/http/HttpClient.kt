package nl.vanwollingen.alerticorn.http

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient.newBuilder
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.noBody
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse
import java.time.Duration

object HttpClient {

    private val client = newBuilder().connectTimeout(Duration.ofSeconds(5)).build()

    @JvmStatic
    fun exchange(
        uri: String, method: String, headers: Map<String, List<String>> = emptyMap(), body: String? = null
    ): HttpResponse<String> {
        try {
            val requestBuilder = HttpRequest.newBuilder().uri(URI.create(uri)).timeout(Duration.ofSeconds(5))

            headers.forEach { (key, values) ->
                values.forEach { value -> requestBuilder.header(key, value) }
            }

            val bodyPublisher = if (body != null) ofString(body) else noBody()

            val request = when (method.uppercase()) {
                "GET" -> requestBuilder.GET().build()
                "POST" -> requestBuilder.POST(bodyPublisher).build()
                "PUT" -> requestBuilder.PUT(bodyPublisher).build()
                "PATCH" -> requestBuilder.method("PATCH", bodyPublisher).build()
                "DELETE" -> requestBuilder.method("DELETE", bodyPublisher).build()
                "HEAD" -> requestBuilder.HEAD().build()
                "OPTIONS" -> requestBuilder.method("OPTIONS", noBody()).build()

                else -> throw IllegalArgumentException("Unsupported HTTP method: $method")
            }

            return client.send(request, HttpResponse.BodyHandlers.ofString())

        } catch (e: Exception) {
            throw IOException("HTTP request failed for $method $uri: ${e.message}", e)
        }
    }

    @JvmStatic
    fun get(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
    ) = exchange(uri, "GET", headers)

    @JvmStatic
    fun post(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "POST", headers, body)

    @JvmStatic
    fun put(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "PUT", headers, body)

    @JvmStatic
    fun patch(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "PATCH", headers, body)

    @JvmStatic
    fun delete(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "DELETE", headers, body)

    @JvmStatic
    fun head(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
    ) = exchange(uri, "HEAD", headers)

    @JvmStatic
    fun options(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
    ) = exchange(uri, "OPTIONS", headers)
}