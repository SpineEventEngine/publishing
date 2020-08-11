package io.spine.publishing.github

import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors.joining

/**
 * An HTTP request to the GitHub REST API.
 *
 * Requires authorization using a [JWT token][GitHubJwt].
 */
abstract class GitHubApiRequest<T>(val jwt: GitHubJwt,
                                   val url: String,
                                   val method: RequestMethod = RequestMethod.GET) {

    fun perform(): String {
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
            return responseText
        }
    }

    abstract fun fetch(responseText: String): T
}

enum class RequestMethod {
    GET {
        override fun toString() = "GET"
    },
    POST {
        override fun toString() = "POST"
    }
}
