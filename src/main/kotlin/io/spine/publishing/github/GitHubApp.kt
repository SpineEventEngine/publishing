/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.publishing.github

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import java.io.StringReader

/** The ID of the GitHub App. */
typealias AppId = String

/**
 * A GitHub App.
 *
 * GitHub Apps can perform Git operations with remote repositories hosted on GitHub.
 *
 * [See here](https://developer.github.com/apps/) for GitHub Apps documentation.
 *
 * @param id the ID of the GitHub App
 * @param jwtFactory a factory of JWTs that authorize GitHub API requests
 */
class GitHubApp(val id: AppId, private val jwtFactory: JwtFactory) {

    /**
     * Obtains an installation ID of this App.
     *
     * The installation ID is required to create an App installation token which
     * is used for authorization.
     */
    fun fetchSingleInstallation(): AppInstallation {
        return FetchAppInstallations
                .forAppWithSingleInstallation(this)
                .perform()
    }

    /** Returns a factory of tokens that can authorize Git operations on behalf of this App. */
    fun tokenFactory() = AppInstallationTokens(this)

    /**
     * Returns a new JWT that can authorize GitHub API requests using the permissions of this App.
     */
    internal fun newJwt() = jwtFactory.jwtFor(this)
}

/**
 * An installation of a GitHub App.
 *
 * GitHub Apps can be installed for an organization or for a repository.
 *
 * Each distinct installation has a different ID.
 *
 * [See here](https://docs.github.com/en/developers/apps/installing-github-apps) for GitHub App
 * installation information.
 *
 * @param id the string value of the installation ID
 */
data class AppInstallation(val id: String)

private typealias PickInstallation = (JsonArray<JsonObject>) -> AppInstallation

/**
 * Fetches the installation ID for the GitHub App defined by the JWT.
 *
 * The App may have many installations. [pickInstallationFn] is used to pick the installation
 * to return from [perform].
 *
 * @param app an app for which to fetch the installations
 * @param pickInstallationFn a function to select the necessary installation
 * @param httpTransport an HTTP transport to use
 */
class FetchAppInstallations private constructor(app: GitHubApp,
                                                private val pickInstallationFn: PickInstallation,
                                                httpTransport: HttpTransport)
    : GitHubApiRequest<AppInstallation>(
        url = URL,
        jwt = app.newJwt(),
        httpTransport = httpTransport
) {

    companion object {

        private const val URL: String = "https://api.github.com/app/installations"

        private val singleInstallation: PickInstallation = {
            when (it.size) {
                0 -> throw IllegalStateException("The app has zero installations.")
                1 -> parseInstallationId(it[0])
                else -> throw IllegalStateException("The app was installed more than once. " +
                        "Installations: `$it`.")
            }
        }

        private fun parseInstallationId(jsonObject: JsonObject): AppInstallation {
            val rawId = jsonObject["id"] as Int
            return AppInstallation(rawId.toString())
        }

        /**
         * Returns a request that upon success returns the ID of the *only* installation returned
         * by the GitHub API.
         *
         * If the GitHub App is known to be installed zero or multiple times, use another
         * [PickInstallation] function.
         *
         * @param app a GitHub App for which the installation is fetched
         * @param httpTransport an HTTP transport to use; may be overridden for tests
         */
        fun forAppWithSingleInstallation(app: GitHubApp,
                                         httpTransport: HttpTransport = NetHttpTransport())
                : FetchAppInstallations =
                FetchAppInstallations(app, singleInstallation, httpTransport)
    }

    override fun parseResponse(responseText: String): AppInstallation {
        val reader = StringReader(responseText)

        @Suppress("UNCHECKED_CAST" /*
                                    * Successful responses from the
                                    * `installations` endpoint are known to return
                                    * an array of JSONs.
                                    */)
        val installations = Klaxon().parseJsonArray(reader) as JsonArray<JsonObject>
        return pickInstallationFn(installations)
    }
}
