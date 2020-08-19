/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.publishing

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_MULTIPLE_CHOICES
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import io.spine.publishing.gradle.Version

/**
 * An artifact in [Spine CloudRepo](https://spine.mycloudrepo.io/public/repositories/)
 * artifact repository.
 */
class SpineCloudRepoArtifact(private val artifact: Artifact,
                             transport: HttpTransport = NetHttpTransport()) {

    private val requestFactory = transport.createRequestFactory()

    /**
     * Returns whether an artifact of the specified version is present in the artifact repository.
     */
    fun isPublished(version: Version): Boolean {
        val url = url(version)
        val response = requestFactory.buildGetRequest(GenericUrl(url))
                .setThrowExceptionOnExecuteError(false)
                .execute()
        return when (response.statusCode) {
            in (STATUS_CODE_OK until STATUS_CODE_MULTIPLE_CHOICES) -> true
            404 -> false
            else -> throw IllegalStateException("Could not determine whether artifact " +
                    "${artifact.artifactName} is published: mycloudrepo returned an unexpected" +
                    "response. " +
                    "Code: `${response.statusCode}`, " +
                    "response: ${response.content.bufferedReader().lines()}")
        }
    }

    /**
     * Construct a URL to this artifact of the specified version.
     *
     * Visible for testing.
     */
    internal fun url(version: Version): String {
        val resultBuilder = StringBuilder()
        resultBuilder.append("https://")

        val urlParts = mutableListOf("spine.mycloudrepo.io",
                "public",
                "repositories",
                "releases"
        )
        urlParts.addAll(artifact.groupId.parts)
        urlParts.add(artifact.artifactName)
        urlParts.add(version.toString())
        val url = urlParts.joinToString(separator = "/", postfix = "/")
        resultBuilder.append(url)
        return resultBuilder.toString()
    }
}
