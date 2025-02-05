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

    fun get(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
    ) = exchange(uri, "GET", headers)

    fun post(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "POST", headers, body)

    fun put(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "PUT", headers, body)

    fun patch(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "PATCH", headers, body)

    fun delete(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "DELETE", headers, body)

    fun head(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
    ) = exchange(uri, "HEAD", headers)

    fun options(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
    ) = exchange(uri, "OPTIONS", headers)
}