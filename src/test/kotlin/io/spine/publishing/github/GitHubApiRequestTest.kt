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
import io.spine.publishing.github.given.GitHubRequestsTestEnv.mockJwt
import io.spine.publishing.github.given.GitHubRequestsTestEnv.transportThatRespondsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

@DisplayName("`GitHubApiRequest` should")
class GitHubApiRequestTest {

    @ParameterizedTest
    @DisplayName("throw an exception upon an erroneous status code")
    @MethodSource("erroneousStatuses")
    fun badStatusCode(code: Int) {
        val transport = transportThatRespondsWith { response -> response.setStatusCode(code) }
        val mockRequest = object : GitHubApiRequest<Unit>(
                "https://github.com/mock_endpoint",
                jwt = mockJwt,
                httpTransport = transport
        ) {
            override fun parseResponse(responseText: String) = Unit
        }
        assertThrows<IllegalStateException> {
            mockRequest.perform()
        }
    }

    @ParameterizedTest
    @DisplayName("throw an exception upon a request to an invalid URL")
    @ValueSource(strings = [
        "localhost",
        "localhost/fake_endpoint",
        "github.com",
        "api.github.com/installations",
        "127.0.0.1",
        "192.156.21.15/fake_endpoint",
        "2002:cb0a:3cdd:1::1"])
    fun badUrls(badUrl: String) {
        val transport = transportThatRespondsWith { response ->
            response.setContent("OK").setStatusCode(200)
        }
        val mockRequest =
                object : GitHubApiRequest<Unit>(badUrl,
                        jwt = mockJwt,
                        httpTransport = transport) {
                    override fun parseResponse(responseText: String) = Unit

                }
        assertThrows<IllegalArgumentException> {
            mockRequest.perform()
        }
    }

    @Test
    @DisplayName("parse a response from a successful response")
    fun successfulResponse() {
        val hardcodedResponse = "OK"
        val transport = transportThatRespondsWith { response ->
            response.setContent(hardcodedResponse)
                    .setStatusCode(200)
        }

        val mockRequest = object : GitHubApiRequest<String>(
                "https://api.github.com/valid_url",
                jwt = mockJwt,
                httpTransport = transport
        ) {
            override fun parseResponse(responseText: String): String = responseText
        }

        assertThat(mockRequest.perform()).isEqualTo(hardcodedResponse)
    }


    @Suppress("unused" /* Methods are invoked by the parameterized tests via reflection. */)
    companion object {

        @JvmStatic
        fun erroneousStatuses(): Stream<Arguments> = (300..599).toList().stream().map { arguments(it) }
    }
}
