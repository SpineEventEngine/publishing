package io.spine.publishing.github

import com.beust.klaxon.Klaxon
import io.spine.publishing.git.Token
import io.spine.publishing.github.RequestMethod.POST
import java.io.StringReader

/**
 * A request to fetch the installation access token for a GitHub App installation.
 *
 * The token may be used to authorize operations permitted to the App installation with
 * the specified ID.
 *
 * Tokens fetched by this request expire in an hour after being fetched.
 */
class FetchApplicationToken(jwt: GitHubJwt, installationId: AppInstallationId) :
        GitHubApiRequest<Token>(jwt, url(installationId), POST) {

    companion object {
        private fun url(installationId: AppInstallationId): String =
                "https://api.github.com/app/installations/${installationId.value}/access_tokens"
    }

    override fun parseResponse(responseText: String): Token {
        val result = Klaxon().parseJsonObject(StringReader(responseText))["token"] as String
        return Token(result)
    }
}
