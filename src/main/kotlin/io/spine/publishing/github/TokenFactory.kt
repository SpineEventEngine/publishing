package io.spine.publishing.github

import io.spine.publishing.git.Token
import java.nio.file.Path

/**
 * A factory for tokens that can be used to authorize remote Git operations.
 */
class TokenFactory(private val privateKeyPath: Path, private val appId: AppId) {

    /**
     * Creates a new GitHub App token.
     *
     * Tokens have a lifetime of 1 hour. After 1 hour, a new token has to be created.
     * When doing so, note the JWT lifetime, described in the [GitHubJwt] documentation.
     */
    fun newToken(): Token {
        val jwt = GitHubJwt.generate(privateKeyPath, appId)
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
     */
    private fun installationId(jwt: GitHubJwt) = FetchAppInstallationId
            .useFirstInstallation(jwt)
            .perform()
}
