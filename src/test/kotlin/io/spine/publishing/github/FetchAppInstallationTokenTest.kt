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
import io.spine.publishing.git.GitHubToken
import io.spine.publishing.github.given.GitHubRequestsTestEnv.mockInstallationId
import io.spine.publishing.github.given.GitHubRequestsTestEnv.mockJwtFactory
import io.spine.publishing.github.given.GitHubRequestsTestEnv.successfulApplicationTokenResponse
import io.spine.publishing.github.given.GitHubRequestsTestEnv.transportThatRespondsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("`FetchAppInstallationToken` should")
class FetchAppInstallationTokenTest {

    @Test
    @DisplayName("parse a token from a successful response")
    fun successfulResponse() {
        val transport = transportThatRespondsWith { response ->
            response.setStatusCode(200)
                    .setContent(successfulApplicationTokenResponse)
        }

        val expectedExpirationTime = Instant.parse("2020-08-13T15:01:37Z")
        val parsedToken = FetchAppInstallationToken(mockJwtFactory, mockInstallationId, transport)
                .perform()
        assertThat(parsedToken).isEqualTo(GitHubToken("mock_token_value", expectedExpirationTime))
    }
}
