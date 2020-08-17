package io.spine.publishing.github

import com.beust.klaxon.Klaxon
import com.google.api.client.http.HttpMethods
import com.google.api.client.http.HttpTransport
import io.spine.publishing.git.GitHubToken
import java.io.StringReader
import java.time.Instant

/**
 * A factory of GitHub tokens.
 *
 * GitHub tokens authorize Git operations.
 */
interface TokenFactory {

    /**
     * Creates a new GitHub App token.
     */
    fun newToken(): GitHubToken
}

/**
 * A factory for tokens that can be used to authorize GitHub API requests on behalf of a GitHub App.
 *
 * The tokens are fetched using the GitHub REST API.
 *
 * @param app a GitHub App to fetch the tokens for
 */
class AppInstallationTokens(private val app: GitHubApp) : TokenFactory {

    /**
     * Creates a new GitHub App token.
     *
     * Tokens have a lifetime of 1 hour. After 1 hour, a new token has to be created.
     */
    override fun newToken(): GitHubToken {
        val jwt = app.newJwt()
        val installation = app.fetchSingleInstallation()
        return FetchAppInstallationToken(jwt, installation).perform()
    }
}

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
     * @param installation the installation ID of the GitHub App to fetch a token for
     * @param jwt a JWT that authorizes the request
     * @param httpTransport the HTTP transport to use
     */
    constructor(installation: AppInstallation, jwt: GitHubJwt, httpTransport: HttpTransport) :
            super(url(installation), HttpMethods.POST, jwt, httpTransport)

    /**
     * Creates a new request to fetch the GitHub App installation token.
     *
     * The fetched token can be used to authorize operations with GitHub, see [GitHubToken].
     *
     * @param jwt a JWT that authorizes the request
     * @param installation the installation ID of the GitHub App to fetch a token for
     */
    constructor(jwt: GitHubJwt, installation: AppInstallation) :
            super(url(installation), jwt = jwt, method = HttpMethods.POST)

    companion object {
        private fun url(installation: AppInstallation): String =
                "https://api.github.com/app/installations/${installation.id}/access_tokens"
    }

    override fun parseResponse(responseText: String): GitHubToken {
        val payload = Klaxon().parseJsonObject(StringReader(responseText))
        val tokenValue = payload["token"] as String
        val expiresAt = Instant.parse(payload["expires_at"] as String)
        return GitHubToken(tokenValue, expiresAt) {this.perform().value}
    }
}
