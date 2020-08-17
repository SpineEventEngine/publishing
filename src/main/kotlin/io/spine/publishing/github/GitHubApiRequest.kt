package io.spine.publishing.github

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpMethods
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.common.net.HttpHeaders.ACCEPT
import com.google.common.net.HttpHeaders.AUTHORIZATION

/**
 * An HTTP request to the GitHub REST API.
 *
 * Requires authorization using a [JWT][GitHubJwt].
 *
 * [Parses successful responses][parseResponse] into `T` objects.
 *
 * @param T the type of objects extracted from the HTTP responses
 *
 * @param url a URL that the request is made to
 * @param method an HTTP method used for the request; defaults to "GET";
 * use [com.google.api.client.http.HttpMethods] when passing a method
 * @param jwt a JWT that authorizes GitHub API requests
 * @param httpTransport a transport to use when making the request; can be overridden for tests
 */
abstract class GitHubApiRequest<T>(private val url: String,
                                   private val method: String = HttpMethods.GET,
                                   private val jwt: GitHubJwt,
                                   httpTransport: HttpTransport = NetHttpTransport()) {

    private val requestFactory = httpTransport.createRequestFactory()
    private val backOff: JwtRefreshingBackOff = JwtRefreshingBackOff(3, jwt)

    /**
     * Performs the HTTP request to the [URL][url] using the specified [method] and
     * setting and authorization header to use the [jwt].
     *
     * If the response has a non-error status code, a response text is
     * [parsed into a typed response][parseResponse].
     *
     * Otherwise, an [IllegalStateException] is thrown.
     */
    fun perform(): T {
        val httpHeaders = HttpHeaders()
        httpHeaders[AUTHORIZATION] = "Bearer ${jwt.value}"
        httpHeaders[ACCEPT] = "application/vnd.github.machine-man-preview+json"

        val response = requestFactory
                .buildRequest(method, GenericUrl(url), null)
                .setHeaders(httpHeaders)
                .setThrowExceptionOnExecuteError(false)
                .setUnsuccessfulResponseHandler(backOff)
                .execute()
        val responseText = response.content.bufferedReader().use { it.readText() }
        if (!response.isSuccessStatusCode) {
            throw IllegalStateException("Request to URL `$url` resulted in a response with " +
                    "`${response.statusCode}`. Response text: `${responseText}`.")
        }

        return parseResponse(responseText)
    }

    /**
     * Parses the `T` from a raw HTTP response string.
     *
     * The [responses][responseText] are guaranteed to have a non-error status code. Therefore,
     * the extenders should expect only successful API responses.
     *
     * Throws an exception if `T` could not be parsed.
     *
     * @param responseText text of a successful GitHub REST API response
     */
    protected abstract fun parseResponse(responseText: String): T
}
