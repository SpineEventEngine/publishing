package io.spine.publishing.github

import com.beust.klaxon.Klaxon
import io.spine.publishing.github.RequestMethod.POST
import java.io.StringReader

class FetchApplicationToken(jwt: GitHubJwt, installationId: AppInstallationId) :
        GitHubApiRequest<ApplicationToken>(jwt, url(installationId), POST) {

    companion object {
        fun url(installationId: AppInstallationId): String =
                "https://api.github.com/app/installations/${installationId.value}/access_tokens"
    }

    override fun fetch(responseText: String): ApplicationToken {
        val result = Klaxon().parseJsonObject(StringReader(responseText))["token"] as String
        return ApplicationToken(result)
    }
}

data class ApplicationToken(val value: String)
