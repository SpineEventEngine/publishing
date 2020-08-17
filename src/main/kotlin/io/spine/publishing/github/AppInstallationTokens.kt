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
 * To fetch a GitHub App, a JWT is needed.
 *
 * If the [jwt] expires, it is [refreshed][GitHubJwt.refresh].
 *
 * @param jwt a JWT that authorizes the fetching of the token
 */
class AppInstallationTokens(private val jwt: GitHubJwt) : TokenFactory {

    /**
     * Creates a new GitHub App token.
     *
     * Tokens have a lifetime of 1 hour. After 1 hour, a new token has to be created.
     */
    override fun newToken(): GitHubToken {
        val installationId = installationId(jwt)
        return FetchAppInstallationToken(jwt, installationId).perform()
    }

    /**
     * Obtains an installation ID of this App.
     *
     * The installation ID is required to create an App installation token which
     * is used for authorization.
     *
     * Note: the installation ID is immutable and can be cached.
     * Currently, `TokenFactory` fetches it every time, but it doesn't necessarily have to.
     *
     * @param jwt a JWT that authorizes the fetching of the installation ID
     */
    private fun installationId(jwt: GitHubJwt) = FetchAppInstallationId
            .forAppWithSingleInstallation(jwt)
            .perform()
}
