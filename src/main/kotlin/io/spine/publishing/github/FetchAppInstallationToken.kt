package io.spine.publishing.github

import com.beust.klaxon.Klaxon
import com.google.api.client.http.HttpMethods.POST
import com.google.api.client.http.HttpTransport
import io.spine.publishing.git.GitHubToken
import java.io.StringReader
import java.time.Instant

/**
 * A request to fetch the installation access token for a GitHub App installation.
 *
 * The token may be used to authorize operations permitted to the App installation with
 * the specified ID.
 *
 * Tokens fetched by this request expire in an hour after being fetched.
 */
class FetchAppInstallationToken : GitHubApiRequest<GitHubToken> {

    /**
     * Creates a new request to fetch the GitHub App installation token.
     *
     * This constructor should be used for tests to mock the HTTP transport.
     *
     * @param jwt JWT to authorize the token fetch
     * @param installationId the installation ID of the GitHub App to fetch a token for
     * @param httpTransport the HTTP transport to use
     */
    constructor(jwt: GitHubJwt, installationId: AppInstallationId, httpTransport: HttpTransport) :
            super(jwt, url(installationId), POST, httpTransport)

    /**
     * Creates a new request to fetch the GitHub App installation token.
     *
     * The fetched token can be used to authorize operations with GitHub, see [GitHubToken].
     *
     * @param jwt JWT to authorize the token fetch
     * @param installationId the installation ID of the GitHub App to fetch a token for
     */
    constructor(jwt: GitHubJwt, installationId: AppInstallationId) :
            super(jwt, url(installationId), method = POST)

    companion object {
        private fun url(installationId: AppInstallationId): String =
                "https://api.github.com/app/installations/${installationId.value}/access_tokens"
    }

    override fun parseResponse(responseText: String): GitHubToken {
        val payload = Klaxon().parseJsonObject(StringReader(responseText))
        val tokenValue = payload["token"] as String
        val expiresAt = Instant.parse(payload["expires_at"] as String)
        return GitHubToken(tokenValue, expiresAt)
    }
}
