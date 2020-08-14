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

package io.spine.publishing.github.given

import com.google.api.client.http.LowLevelHttpRequest
import com.google.api.client.http.LowLevelHttpResponse
import com.google.api.client.testing.http.MockHttpTransport
import com.google.api.client.testing.http.MockLowLevelHttpRequest
import com.google.api.client.testing.http.MockLowLevelHttpResponse
import io.spine.publishing.github.AppInstallationId
import io.spine.publishing.github.GitHubJwt
import io.spine.publishing.github.JwtFactory
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

/**
 * Utilities for testing GitHub related requests.
 */
object GitHubRequestsTestEnv {

    fun transportThatRespondsWith(fn: SetupMockHttpResponse): MockHttpTransport =
            object : MockHttpTransport() {
                override fun buildRequest(method: String, url: String): LowLevelHttpRequest {
                    return object : MockLowLevelHttpRequest() {
                        override fun execute(): LowLevelHttpResponse {
                            val response = MockLowLevelHttpResponse()
                            return fn(response)
                        }
                    }
                }
            }

    val mockJwtFactory = object : JwtFactory {
        override fun newJwt(): GitHubJwt =
                GitHubJwt("abcdefgciOiWAUrIsNiJ9.eyJpY3023123O1czMjcxLsVdIaV2vQA2ZD",
                        Instant.now().plus(1, DAYS))
    }


    val mockInstallationId = AppInstallationId("30235051")

    /**
     * A response from the GitHub `installations/<id>/access_tokens/` endpoint.
     *
     * The returned token is "mock_token_value",
     * which expires at `Instant.parse("2020-08-13T15:01:37Z")`.
     */
    const val successfulApplicationTokenResponse = """{
  "token": "mock_token_value",
  "expires_at": "2020-08-13T15:01:37Z",
  "permissions": {
    "contents": "write",
    "metadata": "read",
    "repository_hooks": "write"
  },
  "repository_selection": "all"
}"""

    val successfulAppInstallationResponse =
            resourceFileToString("successful_app_installation_response.txt")

    val appInstalledMoreThanOnce = resourceFileToString("app_installed_more_than_once.txt")

    private fun resourceFileToString(fileName: String): String {
        val stream = javaClass.classLoader.getResourceAsStream(fileName)
        return stream!!.bufferedReader().readText()
    }
}

typealias SetupMockHttpResponse = (MockLowLevelHttpResponse) -> MockLowLevelHttpResponse
