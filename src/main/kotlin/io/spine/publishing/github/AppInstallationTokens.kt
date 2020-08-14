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
 * @param jwtFactory a factory of JWTs to use to fetch the application tokens.
 */
class AppInstallationTokens(private val jwtFactory: JwtFactory) : TokenFactory {

    /**
     * Creates a new GitHub App token.
     *
     * Tokens have a lifetime of 1 hour. After 1 hour, a new token has to be created.
     * When doing so, note the JWT lifetime, described in the [GitHubJwt] documentation.
     */
    override fun newToken(): GitHubToken {
        val installationId = installationId(jwtFactory)
        return FetchAppInstallationToken(jwtFactory, installationId).perform()
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
     * @param jwtFactory a factory of JWTs to authorize the fetch of the installation ID
     */
    private fun installationId(jwtFactory: JwtFactory) = FetchAppInstallationId
            .useFirstInstallation(jwtFactory)
            .perform()
}
