package io.spine.publishing.github

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors.joining

class FetchAppId(jwt: GitHubJwt) : GitHubApiRequest<AppInstallationId>(jwt, URL) {

    companion object {

        private val URL: String = "https://api.github.com/app/installations"

        fun queryGhApi(jwt: GitHubJwt): String {
            val url = URL("https://api.github.com/app/installations")
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer ${jwt.value}")
                setRequestProperty("Accept", "application/vnd.github.machine-man-preview+json")
                inputStream.bufferedReader()
                        .use {
                            return it.lines()
                                    .collect(joining())
                        }
            }
        }
    }

    override fun fetch(responseText: String): AppInstallationId {
        val reader = StringReader(responseText)
        val installation = Klaxon().parseJsonArray(reader)[0] as JsonObject
        val installationId = installation["id"] as Int
        return AppInstallationId(installationId.toString())
    }
}

data class AppInstallationId(val value: String)
