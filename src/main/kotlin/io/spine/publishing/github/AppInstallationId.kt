package io.spine.publishing.github

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.io.StringReader

data class AppInstallationId(val value: String)

class FetchAppInstallationId(jwt: GitHubJwt) : GitHubApiRequest<AppInstallationId>(jwt, URL) {

    companion object {

        private const val URL: String = "https://api.github.com/app/installations"
    }

    override fun parseResponse(responseText: String): AppInstallationId {
        val reader = StringReader(responseText)
        val installation = Klaxon().parseJsonArray(reader)[0] as JsonObject
        val installationId = installation["id"] as Int
        return AppInstallationId(installationId.toString())
    }
}
