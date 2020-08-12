package io.spine.publishing.github

import io.spine.publishing.git.Token
import java.nio.file.Path

/**
 * A factory for tokens that can be used to authorize remote Git operations.
 */
class TokenFactory(private val privateKeyPath: Path, private val appId: AppId) {

    /**
     * Creates a new GitHub App token.
     */
    fun newToken(): Token {
        val jwt = GitHubJwt.generate(privateKeyPath, appId)
        val installationId = installationId(jwt)
        return FetchApplicationToken(jwt, installationId).perform()
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
