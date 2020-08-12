package io.spine.publishing.github

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
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
 * @param jwt JWT to authorize the request
 * @param pickInstallationFn a function to select the necessary installation
 */
class FetchAppInstallationId private constructor(jwt: GitHubJwt,
                                                 private val pickInstallationFn: PickInstallation)
    : GitHubApiRequest<AppInstallationId>(jwt, URL) {

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
         * Returns a request that upon success returns the ID of the first installation returned
         * by GitHub.
         *
         * This is useful if the app is known to be installed only once. For other apps a more
         * robust choice mechanism is required.
         */
        fun useFirstInstallation(jwt: GitHubJwt): FetchAppInstallationId =
                FetchAppInstallationId(jwt, pickFirst)
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
