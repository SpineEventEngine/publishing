package io.spine.publishing.github

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import java.io.StringReader

/**
 * ID of an installation of a GitHub App.
 *
 * GitHub Apps can be installed for an organization or for a repository.
 *
 * Each distinct installation has a different ID.
 *
 * [See here](https://docs.github.com/en/developers/apps/installing-github-apps) for GitHub App
 * installation information.
 *
 * @param value the string value of the installation ID
 */
data class AppInstallationId(val value: String)

private typealias PickInstallation = (JsonArray<JsonObject>) -> JsonObject

/**
 * Fetches the installation ID for the GitHub App defined by the JWT.
 *
 * The App may have many installations. [pickInstallationFn] is used to pick the installation
 * to return from [perform].
 *
 * @param pickInstallationFn a function to select the necessary installation
 * @param jwt a JWT that authorizes GitHub REST API operations
 * @param httpTransport an HTTP transport to use
 */
class FetchAppInstallationId private constructor(private val pickInstallationFn: PickInstallation,
                                                 jwt: GitHubJwt,
                                                 httpTransport: HttpTransport)
    : GitHubApiRequest<AppInstallationId>(
        url = URL,
        jwt = jwt,
        httpTransport = httpTransport
) {

    companion object {

        private const val URL: String = "https://api.github.com/app/installations"

        private val singleInstallation: PickInstallation = {
            when (it.size) {
                0 -> throw IllegalStateException("The app has zero installations.")
                1 -> it[0]
                else -> throw IllegalStateException("The app was installed more than once. " +
                        "Installations: `$it`.")
            }
        }

        /**
         * Returns a request that upon success returns the ID of the *only* installation returned
         * by the GitHub API.
         *
         * If the GitHub App is known to be installed zero or multiple times, use another
         * [PickInstallation] function.
         *
         * @param jwt a JWT that authorizes GitHub API requests
         * @param httpTransport an HTTP transport to use; may be overridden for tests
         */
        fun forAppWithSingleInstallation(jwt: GitHubJwt,
                                         httpTransport: HttpTransport = NetHttpTransport())
                : FetchAppInstallationId =
                FetchAppInstallationId(singleInstallation, jwt, httpTransport)
    }

    override fun parseResponse(responseText: String): AppInstallationId {
        val reader = StringReader(responseText)

        @Suppress("UNCHECKED_CAST" /*
                                    * Successful responses from the
                                    * `installations` endpoint are known to return
                                    * an array of JSONs.
                                    */)
        val installations = Klaxon().parseJsonArray(reader) as JsonArray<JsonObject>
        val installation = pickInstallationFn(installations)
        val installationId = installation["id"] as Int
        return AppInstallationId(installationId.toString())
    }
}
