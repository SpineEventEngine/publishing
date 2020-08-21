package io.spine.publishing.github

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpMethods
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.common.net.HttpHeaders.ACCEPT
import io.spine.publishing.debug

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
 * @param backOff the back-off policy to use; defaults to a back-off that retries thrice
 */
abstract class GitHubApiRequest<T>(
        private val url: String,
        private val method: String = HttpMethods.GET,
        private val jwt: GitHubJwt,
        private val httpTransport: HttpTransport = NetHttpTransport(),
        private val backOff: JwtRefreshingBackOff = JwtRefreshingBackOff(3, jwt)) {

    private val requestFactory = httpTransport.createRequestFactory()

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
        httpHeaders[ACCEPT] = "application/vnd.github.machine-man-preview+json"

        val request = requestFactory
                .buildRequest(method, GenericUrl(url), null)
        jwt.authorize(request)
        debug().log("Sending a `$method` to `$url`. Headers: `$httpHeaders`.")
        val response = request
                .setHeaders(httpHeaders)
                .setThrowExceptionOnExecuteError(false)
                .setUnsuccessfulResponseHandler(backOff)
                .execute()
        val responseText = response.content.bufferedReader().use { it.readText() }
        debug().log("Got the response with code `${response.statusCode}`.")
        if (!response.isSuccessStatusCode) {
            throw IllegalStateException("Request to URL `$url` resulted in a response with " +
                    "`${response.statusCode}`. Response text: `${responseText}`.")
        }
        backOff.reset()
        return parseResponse(responseText)
    }

    /**
     * Parses the `T` from a raw HTTP response string.
     *
     * The [responses][responseText] are guaranteed to have a non-error status code. Therefore,
     * the extenders should expect only successful API responses.
     *
     * Descendants should throw an exception if the result could not be parsed.
     *
     * @param responseText text of a successful GitHub REST API response
     */
    protected abstract fun parseResponse(responseText: String): T
}
