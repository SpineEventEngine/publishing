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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_NOT_FOUND
import com.google.api.client.http.HttpTransport
import io.spine.publishing.github.given.GitHubRequestsTestEnv.transportThatRespondsWith
import io.spine.publishing.gradle.Version
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

@DisplayName("`SpineCloudRepoArtifact` should")
class SpineCloudRepoArtifactTest {

    @Suppress("unused" /* The static methods are used via reflection. */)
    companion object {

        private val artifact = Artifact(GroupId("com", "acme"), "tnt")
        private val mockVersion = Version(1, 1, 1)

        fun mockArtifact(transport: HttpTransport) = SpineCloudRepoArtifact(artifact, transport)

        @JvmStatic
        fun badCodes() = (500..599).map { arguments(it) }

        @JvmStatic
        fun goodCodes() = (200..299).map { arguments(it) }
    }

    @Test
    @DisplayName("build a correct link to the cloud repo")
    fun correctUrl() {
        val expected = "https://spine.mycloudrepo.io/public/repositories/releases/io/spine/spine-core/1.5.21/"

        val artifact = Artifact(GroupId("io", "spine"), "spine-core")
        val actual = SpineCloudRepoArtifact(artifact).url(Version(1, 5, 21))
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    @DisplayName("tell that the artifact is not published upon seeing a 404")
    fun notFound() {
        val transport = transportThatRespondsWith { it.setStatusCode(STATUS_CODE_NOT_FOUND) }

        val isPublished = mockArtifact(transport).isPublished(mockVersion)
        assertThat(isPublished).isFalse()
    }

    @ParameterizedTest
    @DisplayName("throw an exception upon seeing a server error status code")
    @MethodSource("badCodes")
    fun statusCode500Plus(code: Int) {
        val transport = transportThatRespondsWith { it.setStatusCode(code) }
        assertThrows<IllegalStateException> {
            mockArtifact(transport).isPublished(mockVersion)
        }
    }

    @ParameterizedTest
    @DisplayName("tell that the artifact is published upon seeing a successful code")
    @MethodSource("goodCodes")
    fun successfulStatusCode(code: Int) {
        val transport = transportThatRespondsWith { it.setStatusCode(code) }
        val isPublished = mockArtifact(transport).isPublished(mockVersion)
        assertThat(isPublished).isTrue()
    }
}
