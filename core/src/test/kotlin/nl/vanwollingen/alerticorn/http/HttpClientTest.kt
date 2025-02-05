package nl.vanwollingen.alerticorn.http

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.matching.AbsentPattern
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.IOException

class HttpClientTest {

    companion object {
        @RegisterExtension
        @JvmField
        val wireMock: WireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build()
    }

    @Test
    fun `it should do a GET and pass the headers`() {
        val path = "/get-with-header"
        val responseBody = """{"message": "Success"}"""
        wireMock.stubFor(
            get(urlEqualTo(path))
                .withHeader("Accept", EqualToPattern("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )
        val uri = "${wireMock.baseUrl()}$path"

        val response = HttpClient.get(
            uri = uri,
            headers = mapOf("Accept" to listOf("application/json"))
        )

        response.statusCode() shouldBe 200
        response.body() shouldBe responseBody
        response.headers().allValues("Content-Type") shouldBe listOf("application/json")
    }

    @Test
    fun `it should PUT`() {
        val path = "/put-with-body"
        val putBody = """{"message": "Hello"}"""
        val responseBody = """{"message": "Success"}"""
        wireMock.stubFor(
            put(urlEqualTo(path))
                .withHeader("Content-Type", EqualToPattern("application/json"))
                .withRequestBody(EqualToJsonPattern(putBody, false, false))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )
        val uri = "${wireMock.baseUrl()}$path"

        val response = HttpClient.put(
            uri = uri,
            body = putBody,
            headers = mapOf(
                "Content-Type" to listOf("application/json")
            )
        )

        response.statusCode() shouldBe 200
        response.body() shouldBe responseBody
        response.headers().allValues("Content-Type") shouldBe listOf("application/json")
    }

    @Test
    fun `it should DELETE`() {
        val path = "/delete"
        val deleteBody = """{"message": "Hello"}"""
        val responseBody = """{"message": "Success"}"""
        wireMock.stubFor(
            delete(urlEqualTo(path))
                .withHeader("Content-Type", EqualToPattern("application/json"))
                .withRequestBody(EqualToJsonPattern(deleteBody, false, false))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )
        val uri = "${wireMock.baseUrl()}$path"

        val response = HttpClient.delete(
            uri = uri,
            body = deleteBody,
            headers = mapOf(
                "Content-Type" to listOf("application/json")
            )
        )

        response.statusCode() shouldBe 200
        response.body() shouldBe responseBody
        response.headers().allValues("Content-Type") shouldBe listOf("application/json")
    }

    @Test
    fun `it should HEAD`() {
        val path = "/head"
        val responseBody = """{"message": "Success"}"""
        wireMock.stubFor(
            head(urlEqualTo(path))
                .withHeader("Content-Type", EqualToPattern("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )
        val uri = "${wireMock.baseUrl()}$path"

        val response = HttpClient.head(
            uri = uri,
            headers = mapOf(
                "Content-Type" to listOf("application/json")
            )
        )

        response.statusCode() shouldBe 200
        response.body() shouldBe responseBody
        response.headers().allValues("Content-Type") shouldBe listOf("application/json")
    }

    @Test
    fun `it should OPTIONS`() {
        val path = "/options"
        val responseBody = """{"message": "Success"}"""
        wireMock.stubFor(
            options(urlEqualTo(path))
                .withHeader("Content-Type", EqualToPattern("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )
        val uri = "${wireMock.baseUrl()}$path"

        val response = HttpClient.options(
            uri = uri,
            headers = mapOf(
                "Content-Type" to listOf("application/json")
            )
        )

        response.statusCode() shouldBe 200
        response.body() shouldBe responseBody
        response.headers().allValues("Content-Type") shouldBe listOf("application/json")
    }

    @Test
    fun `it should POST a body if present`() {
        val path = "/post-with-body"
        val postBody = """{"message": "Hello"}"""
        val responseBody = """{"message": "Success"}"""
        wireMock.stubFor(
            post(urlEqualTo(path))
                .withHeader("Content-Type", EqualToPattern("application/json"))
                .withRequestBody(EqualToJsonPattern(postBody, false, false))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )
        val uri = "${wireMock.baseUrl()}$path"

        val response = HttpClient.post(
            uri = uri,
            body = postBody,
            headers = mapOf(
                "Content-Type" to listOf("application/json")
            )
        )

        response.statusCode() shouldBe 200
        response.body() shouldBe responseBody
        response.headers().allValues("Content-Type") shouldBe listOf("application/json")
    }

    @Test
    fun `it should pass multiple headers`() {
        val path = "/patch-with-headers"
        val patchBody = """{"id": "1", "name": "newName"}"""
        wireMock.stubFor(
            patch(urlEqualTo(path))
                .withHeader("Content-Type", EqualToPattern("application/json"))
                .withHeader("X-My-Custom-Header", EqualToPattern("for-update"))
                .withRequestBody(EqualToJsonPattern(patchBody, false, false))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(patchBody)
                )
        )
        val uri = "${wireMock.baseUrl()}$path"

        val response = HttpClient.patch(
            uri = uri,
            body = patchBody,
            headers = mapOf(
                "Content-Type" to listOf("application/json"),
                "X-My-Custom-Header" to listOf("for-update"),
            )
        )

        response.statusCode() shouldBe 200
        response.body() shouldBe patchBody
        response.headers().allValues("Content-Type") shouldBe listOf("application/json")
    }

    @Test
    fun `it should POST, without a provided body`() {
        val path = "/post-without-body"
        val postBody = """{"message": "Hello"}"""
        val responseBody = """{"message": "Success"}"""
        wireMock.stubFor(
            post(urlEqualTo(path))
                .withHeader("Accept", EqualToPattern("application/json"))
                .withRequestBody(AbsentPattern(postBody))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )
        val uri = "${wireMock.baseUrl()}$path"

        val response = HttpClient.post(
            uri = uri,
            headers = mapOf(
                "Accept" to listOf("application/json"),
            )
        )

        response.statusCode() shouldBe 200
        response.body() shouldBe responseBody
        response.headers().allValues("Content-Type") shouldBe listOf("application/json")
    }

    @Test
    @Tag("slow")
    fun `it should throw request timed out exception`() {
        // Arrange
        val path = "/read-timed-out"
        wireMock.stubFor(
            post(urlEqualTo(path))
                .willReturn(
                    aResponse()
                        .withFixedDelay(5001)
                        .withStatus(400)
                )
        )

        val uri = "${wireMock.baseUrl()}$path"

        // Act
        val exception = shouldThrow<IOException> { HttpClient.exchange(uri, "POST", body = """{"key":"value"}""") }

        exception.message shouldStartWith "HTTP request failed for POST "
        exception.message shouldEndWith ": request timed out"
    }

    @Test
    fun `it should throw for unknown HTTP method`() {
        assertThrows<IOException> {  HttpClient.exchange("http://some.url", "GETT") }
    }
}
