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
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.HttpStatusCodes.*
import com.google.api.client.testing.http.MockLowLevelHttpResponse
import io.spine.publishing.github.given.GitHubRequestsTestEnv.mockJwtValue
import io.spine.publishing.github.given.GitHubRequestsTestEnv.mockResponse
import io.spine.publishing.github.given.GitHubRequestsTestEnv.transportWithPresetResponses
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`JwtRefreshingBackOff` should")
class JwtRefreshingBackOffTest {

    private val mockUrl: String = "https://api.github.com/hello"

    @Test
    @DisplayName("not retry if the response code is not `UNAUTHORIZED`")
    fun notRetryNonUnauthorized() {
        var timesRefreshed = 0

        fun jwt(): GitHubJwt = GitHubJwt(mockJwtValue) {
            timesRefreshed++
            jwt()
        }

        val transport = transportWithPresetResponses(listOf(
                mockResponse(STATUS_CODE_FORBIDDEN)
        ))
        val factory = transport.createRequestFactory()
        assertThrows<HttpResponseException> {
            factory.buildGetRequest(GenericUrl(mockUrl))
                    .setUnsuccessfulResponseHandler(JwtRefreshingBackOff(3, jwt()))
                    .execute()
        }

        assertThat(timesRefreshed).isEqualTo(0)
    }

    @Test
    @DisplayName("retry if the response code is `UNAUTHORIZED`")
    fun retryIfUnauthorized() {
        var timesRefreshed = 0

        fun jwt(): GitHubJwt = GitHubJwt(mockJwtValue) {
            timesRefreshed++
            jwt()
        }

        val transport = transportWithPresetResponses(listOf(
                mockResponse(STATUS_CODE_UNAUTHORIZED),
                mockResponse(STATUS_CODE_OK)
        ))

        val createRequestFactory = transport.createRequestFactory()
        createRequestFactory.buildGetRequest(GenericUrl(mockUrl))
                .setUnsuccessfulResponseHandler(JwtRefreshingBackOff(3, jwt()))
                .execute()
        assertThat(timesRefreshed == 1)
    }

    @Test
    @DisplayName("stop trying after exhausting the amount of retries")
    fun stopTrying() {
        var timesRefreshed = 0

        fun jwt(): GitHubJwt = GitHubJwt(mockJwtValue) {
            timesRefreshed++
            jwt()
        }


        val transport = transportWithPresetResponses(listOf(
                mockResponse(STATUS_CODE_UNAUTHORIZED),
                mockResponse(STATUS_CODE_UNAUTHORIZED),
                mockResponse(STATUS_CODE_UNAUTHORIZED),
                mockResponse(STATUS_CODE_OK)
        ))

        val createRequestFactory = transport.createRequestFactory()
        assertThrows<HttpResponseException> {
            createRequestFactory.buildGetRequest(GenericUrl(mockUrl))
                    .setUnsuccessfulResponseHandler(JwtRefreshingBackOff(2, jwt()))
                    .execute()
        }
        assertThat(timesRefreshed == 3)
    }
}
