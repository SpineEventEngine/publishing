package io.spine.publishing.github

import io.spine.publishing.git.GitHubToken

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
