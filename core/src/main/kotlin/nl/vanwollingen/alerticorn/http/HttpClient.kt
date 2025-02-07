package nl.vanwollingen.alerticorn.http

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient.newBuilder
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.noBody
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse
import java.time.Duration

/**
 * A utility object that provides methods for making HTTP requests using the Java HTTP Client.
 * This object supports various HTTP methods (GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS)
 * and allows setting custom headers and request bodies.
 *
 * The client is configured with a 5-second connection timeout.
 */
object HttpClient {

    private val client = newBuilder().connectTimeout(Duration.ofSeconds(5)).build()

    /**
     * Sends an HTTP request using the specified method, URI, headers, and optional body.
     * The method is determined by the [method] parameter, which can be one of:
     * "GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS".
     *
     * If the request method requires a body (e.g., POST, PUT), the [body] parameter is used.
     * Headers are passed as a map, where keys are header names and values are lists of header values.
     *
     * @param uri The URI to which the request is sent.
     * @param method The HTTP method (e.g., "GET", "POST", "PUT").
     * @param headers A map of headers to include in the request.
     * @param body The body of the request, if applicable. If null, no body is sent.
     * @return An [HttpResponse] containing the response body as a string.
     * @throws IOException If an error occurs while sending the request or processing the response.
     */
    @JvmStatic
    fun exchange(
        uri: String, method: String, headers: Map<String, List<String>> = emptyMap(), body: String? = null
    ): HttpResponse<String> {
        try {
            val requestBuilder = HttpRequest.newBuilder().uri(URI.create(uri)).timeout(Duration.ofSeconds(5))

            // Add headers to the request
            headers.forEach { (key, values) ->
                values.forEach { value -> requestBuilder.header(key, value) }
            }

            // Determine the body publisher (if any)
            val bodyPublisher = if (body != null) ofString(body) else noBody()

            // Create the HTTP request based on the method
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

    /**
     * Sends an HTTP GET request to the specified URI with optional headers.
     *
     * @param uri The URI to which the request is sent.
     * @param headers A map of headers to include in the request.
     * @return An [HttpResponse] containing the response body as a string.
     * @throws IOException If an error occurs while sending the request or processing the response.
     */
    @JvmStatic
    fun get(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
    ) = exchange(uri, "GET", headers)

    /**
     * Sends an HTTP POST request to the specified URI with optional headers and body.
     *
     * @param uri The URI to which the request is sent.
     * @param headers A map of headers to include in the request.
     * @param body The body of the POST request.
     * @return An [HttpResponse] containing the response body as a string.
     * @throws IOException If an error occurs while sending the request or processing the response.
     */
    @JvmStatic
    fun post(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "POST", headers, body)

    /**
     * Sends an HTTP PUT request to the specified URI with optional headers and body.
     *
     * @param uri The URI to which the request is sent.
     * @param headers A map of headers to include in the request.
     * @param body The body of the PUT request.
     * @return An [HttpResponse] containing the response body as a string.
     * @throws IOException If an error occurs while sending the request or processing the response.
     */
    @JvmStatic
    fun put(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "PUT", headers, body)

    /**
     * Sends an HTTP PATCH request to the specified URI with optional headers and body.
     *
     * @param uri The URI to which the request is sent.
     * @param headers A map of headers to include in the request.
     * @param body The body of the PATCH request.
     * @return An [HttpResponse] containing the response body as a string.
     * @throws IOException If an error occurs while sending the request or processing the response.
     */
    @JvmStatic
    fun patch(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "PATCH", headers, body)

    /**
     * Sends an HTTP DELETE request to the specified URI with optional headers and body.
     *
     * @param uri The URI to which the request is sent.
     * @param headers A map of headers to include in the request.
     * @param body The body of the DELETE request.
     * @return An [HttpResponse] containing the response body as a string.
     * @throws IOException If an error occurs while sending the request or processing the response.
     */
    @JvmStatic
    fun delete(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: String? = null,
    ) = exchange(uri, "DELETE", headers, body)

    /**
     * Sends an HTTP HEAD request to the specified URI with optional headers.
     *
     * @param uri The URI to which the request is sent.
     * @param headers A map of headers to include in the request.
     * @return An [HttpResponse] containing the response headers.
     * @throws IOException If an error occurs while sending the request or processing the response.
     */
    @JvmStatic
    fun head(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
    ) = exchange(uri, "HEAD", headers)

    /**
     * Sends an HTTP OPTIONS request to the specified URI with optional headers.
     *
     * @param uri The URI to which the request is sent.
     * @param headers A map of headers to include in the request.
     * @return An [HttpResponse] containing the response headers.
     * @throws IOException If an error occurs while sending the request or processing the response.
     */
    @JvmStatic
    fun options(
        uri: String,
        headers: Map<String, List<String>> = emptyMap(),
    ) = exchange(uri, "OPTIONS", headers)
}
