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
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_UNAUTHORIZED
import com.google.api.client.testing.http.MockLowLevelHttpResponse
import io.spine.publishing.github.given.GitHubRequestsTestEnv.mockJwtFactory
import io.spine.publishing.github.given.GitHubRequestsTestEnv.transportWithPresetResponses
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@DisplayName("`JwtRefreshingBackOff` should")
class JwtRefreshingBackOffTest {

    private val mockUrl: String = "https://api.github.com/hello"

    @Test
    @DisplayName("not retry if the response code is not `UNAUTHORIZED`")
    fun notRetryNonUnauthorized() {
        val transport = transportWithPresetResponses(listOf(
                MockLowLevelHttpResponse()
                        .setStatusCode(505)
        ))
        val factory = transport.createRequestFactory()
        assertThrows<HttpResponseException> {
            factory.buildGetRequest(GenericUrl(mockUrl))
                    .setUnsuccessfulResponseHandler(JwtRefreshingBackOff(3, mockJwtFactory))
                    .execute()
        }
    }

    @Test
    @DisplayName("retry if the response code is `UNAUTHORIZED`")
    fun retryIfUnauthorized() {
        val jwtFactory = object : JwtFactory {
            var jwtsCreated = 0

            override fun newJwt(): GitHubJwt {
                jwtsCreated++
                return mockJwtFactory.newJwt()
            }
        }

        val transport = transportWithPresetResponses(listOf(
                MockLowLevelHttpResponse()
                        .setStatusCode(STATUS_CODE_UNAUTHORIZED),
                MockLowLevelHttpResponse()
                        .setStatusCode(200)
        ))

        val createRequestFactory = transport.createRequestFactory()
        createRequestFactory.buildGetRequest(GenericUrl(mockUrl))
                .setUnsuccessfulResponseHandler(JwtRefreshingBackOff(3, jwtFactory))
                .execute()
        assertThat(jwtFactory.jwtsCreated == 1)
    }

    @Test
    @DisplayName("stop trying after exhausting the amount of retries")
    fun stopTrying() {
        val jwtFactory = object : JwtFactory {
            var jwtsCreated = 0

            override fun newJwt(): GitHubJwt {
                jwtsCreated++
                val value = UUID.randomUUID().toString()
                return GitHubJwt(value)
            }
        }

        val transport = transportWithPresetResponses(listOf(
                MockLowLevelHttpResponse()
                        .setStatusCode(STATUS_CODE_UNAUTHORIZED),
                MockLowLevelHttpResponse()
                        .setStatusCode(STATUS_CODE_UNAUTHORIZED),
                MockLowLevelHttpResponse()
                        .setStatusCode(STATUS_CODE_UNAUTHORIZED),
                MockLowLevelHttpResponse()
                        .setStatusCode(200)
        ))

        val createRequestFactory = transport.createRequestFactory()
        assertThrows<HttpResponseException> {
            createRequestFactory.buildGetRequest(GenericUrl(mockUrl))
                    .setUnsuccessfulResponseHandler(JwtRefreshingBackOff(2, jwtFactory))
                    .execute()
        }
        assertThat(jwtFactory.jwtsCreated == 3)
    }
}
