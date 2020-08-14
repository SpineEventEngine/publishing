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

/**
 * Fetches the installation ID for the GitHub App defined by the JWT.
 *
 * The App may have many installations. [pickInstallationFn] is used to pick the installation
 * to return from [perform].
 *
 * @param jwtFactory a factory of JWTs that authroize the GitHub API requests
 * @param pickInstallationFn a function to select the necessary installation
 * @param httpTransport an HTTP transport to use
 */
class FetchAppInstallationId private constructor(jwtFactory: JwtFactory,
                                                 private val pickInstallationFn: PickInstallation,
                                                 httpTransport: HttpTransport)
    : GitHubApiRequest<AppInstallationId>(jwtFactory, URL, httpTransport = httpTransport) {

    companion object {

        private const val URL: String = "https://api.github.com/app/installations"

        private val pickFirst: PickInstallation = {
            when (it.size) {
                0 -> throw IllegalStateException("The app has zero installations.")
                1 -> it[0]
                else -> throw IllegalStateException("The app was installed more than once. " +
                        "Installations: `$it`.")
            }
        }

        /**
         * Returns a request that upon success returns the ID of the first
         * installation returned by the GitHub REST API.
         *
         * This is useful if the app is known to be installed only once. For other apps a more
         * robust choice mechanism is required.
         *
         * @param jwtFactory a factory of JWTs that authorize the GitHub API requests
         * @param httpTransport an HTTP transport to use; may be overridden for tests
         */
        fun useFirstInstallation(jwtFactory: JwtFactory,
                                 httpTransport: HttpTransport = NetHttpTransport())
                : FetchAppInstallationId =
                FetchAppInstallationId(jwtFactory, pickFirst, httpTransport)
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

private typealias PickInstallation = (JsonArray<JsonObject>) -> JsonObject
