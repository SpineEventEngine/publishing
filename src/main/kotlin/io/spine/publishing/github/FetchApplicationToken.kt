package io.spine.publishing.github

import com.beust.klaxon.Klaxon
import io.spine.publishing.git.Token
import io.spine.publishing.github.RequestMethod.POST
import java.io.StringReader

class FetchApplicationToken(jwt: GitHubJwt, installationId: AppInstallationId) :
        GitHubApiRequest<Token>(jwt, url(installationId), POST) {

    companion object {
        fun url(installationId: AppInstallationId): String =
                "https://api.github.com/app/installations/${installationId.value}/access_tokens"
    }

    override fun parseResponse(responseText: String): Token {
        val result = Klaxon().parseJsonObject(StringReader(responseText))["token"] as String
        return Token(result)
    }
}
