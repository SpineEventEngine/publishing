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
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.spine.publishing.git.Add
import io.spine.publishing.git.Checkout
import io.spine.publishing.git.Commit
import io.spine.publishing.git.Push
import io.spine.publishing.gradle.GradleVersionFile
import io.spine.publishing.gradle.Library
import io.spine.publishing.gradle.given.TestEnv
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Paths

@DisplayName("`VersionBumpPullRequest` should")
class VersionUpdateTest {

    @Test
    @DisplayName("emit correct commands when updating a library")
    fun correctCommands(@TempDir tempdir: Path) {
        val baseDirectory = TestEnv.copyDirectory("base", tempdir)
        val library = Library("base", listOf(), baseDirectory)
        val orgName = "TestOrganization"
        val repo = "base"
        val remote = RemoteRepository(library, GitHubRepository(orgName, repo))

        val pullRequest = VersionUpdate(remote, mockCredentials)
        val commands = pullRequest.pushBranch()

        assertThat(commands).hasSize(4)
        assertThat(commands[0]).isInstanceOf(Checkout::class)
        assertThat(commands[1]).isInstanceOf(Add::class)
        assertThat(commands[2]).isInstanceOf(Commit::class)
        assertThat(commands[3]).isInstanceOf(Push::class)

        assertThat((commands[0] as Checkout).checkout.name()).isEqualTo("master")
        assertThat((commands[1] as Add).files.files()).containsOnly(Paths.get(GradleVersionFile.NAME))
        assertThat((commands[3] as Push).pushMetadata.remote.gitHubRepository)
                .isEqualTo(GitHubRepository(orgName, repo))
    }

    private val mockCredentials: CredentialsProvider =
            UsernamePasswordCredentialsProvider("username", "password")
}
