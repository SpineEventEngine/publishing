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

package io.spine.publishing.github

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.spine.publishing.github.given.GitHubRequestsTestEnv.appInstalledMoreThanOnce
import io.spine.publishing.github.given.GitHubRequestsTestEnv.mockJwt
import io.spine.publishing.github.given.GitHubRequestsTestEnv.successfulAppInstallationResponse
import io.spine.publishing.github.given.GitHubRequestsTestEnv.transportThatRespondsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`FetchAppInstallationId` should")
class FetchAppInstallationTest {

    @Test
    @DisplayName("parse an installation ID if the App was installed exactly once")
    fun parseInstallationId() {
        val transport = transportThatRespondsWith { response ->
            response.setStatusCode(200)
                    .setContent(successfulAppInstallationResponse)
        }
        val appId = FetchAppInstallations.forAppWithSingleInstallation(mockApp, transport)
                .perform()
        assertThat(appId).isEqualTo(AppInstallation("42"))
    }

    @Test
    @DisplayName("throw an error if an App wasn't ever installed")
    fun failWasNeverInstalled() {
        val transport = transportThatRespondsWith { response ->
            response.setStatusCode(200)
                    .setContent(appInstalledMoreThanOnce)
        }

        assertThrows<IllegalStateException> {
            FetchAppInstallations.forAppWithSingleInstallation(mockApp, transport).perform()
        }
    }

    @Test
    @DisplayName("throw an error if an App was installed more than once")
    fun failInstalledTooManyTimes() {
        val transport = transportThatRespondsWith { response ->
            response.setStatusCode(200)
                    .setContent("[]")
        }

        assertThrows<IllegalStateException> {
            FetchAppInstallations.forAppWithSingleInstallation(mockApp, transport).perform()
        }
    }

    private val mockApp = GitHubApp("mock_app", object : JwtFactory {
        override fun jwtFor(app: GitHubApp): GitHubJwt = mockJwt()
    })
}
