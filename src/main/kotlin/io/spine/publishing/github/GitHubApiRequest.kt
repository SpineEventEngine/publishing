package io.spine.publishing.github

import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors.joining

/**
 * An HTTP request to the GitHub REST API.
 *
 * Requires authorization using a [JWT token][GitHubJwt].
 *
 * [Parses responses][parseResponse] into `T` objects.
 *
 * @param jwt a JWT that is used for authorization with GitHub
 * @param url a URL that the request is made to
 * @param method an HTTP method used for the request; defaults to "GET"
 * @param T the type of object extracted from HTTP responses
 */
abstract class GitHubApiRequest<T>(private val jwt: GitHubJwt,
                                   private val url: String,
                                   private val method: RequestMethod = RequestMethod.GET) {

    /**
     * Performs the HTTP request to the [url] using the [method] and setting and authorization
     * header to use the [jwt].
     *
     * If the response has a non-error status code, a response text is parsed into a `T`.
     *
     * If the response has an erroneous status code, an [IllegalStateException] is thrown.
     */
    fun perform(): T {
        with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = method.toString()
            setRequestProperty("Authorization", "Bearer ${jwt.value}")
            setRequestProperty("Accept", "application/vnd.github.machine-man-preview+json")
            val responseText = inputStream.bufferedReader()
                    .use {
                        it.lines()
                                .collect(joining())
                    }
            if (responseCode >= 400) {
                throw IllegalStateException("GitHub responded with `$responseCode`. " +
                        "Response text: `$responseText`.")
            }
            return parseResponse(responseText)
        }
    }

    /**
     * Parses the `T` from a raw HTTP response string.
     *
     * The responses are guaranteed to have a non-error status code.
     *
     * Throws an exception if `T` could not be parsed.
     */
    protected abstract fun parseResponse(responseText: String): T
}

/**
 * Type of HTTP method used in [GitHubApiRequest]s.
 */
enum class RequestMethod {
    GET {
        override fun toString() = "GET"
    },
    POST {
        override fun toString() = "POST"
    }
}
