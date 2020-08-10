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

package io.spine.publishing.git

import assertk.assertThat
import assertk.assertions.*
import io.spine.publishing.Library
import io.spine.publishing.gradle.GradleVersionFile
import io.spine.publishing.gradle.given.TestEnv
import io.spine.publishing.operation.UpdateRemote.Companion.updateVersion
import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Paths

@DisplayName("`VersionUpdate` should")
class VersionUpdateTest {

    @Test
    @DisplayName("emit correct commands when updating a library")
    fun correctCommands(@TempDir tempdir: Path) {
        val baseDirectory = TestEnv.copyDirectory("base", tempdir)
        Git.init()
                .setDirectory(baseDirectory.toFile())
                .call()
        val orgName = "TestOrganization"
        val repo = "base"

        val remote = GitHubRepoUrl(orgName, repo)
        val gitRepo = GitRepository(baseDirectory, remote)
        val library = Library("base", listOf(), gitRepo)
        val commands = updateVersion(library, mockToken)

        assertThat(commands).hasSize(4)
        assertThat(commands[0]).isInstanceOf(Checkout::class)
        assertThat(commands[1]).isInstanceOf(StageFiles::class)
        assertThat(commands[2]).isInstanceOf(Commit::class)
        assertThat(commands[3]).isInstanceOf(PushToRemote::class)

        assertThat((commands[0] as Checkout).branch.name()).isEqualTo("master")
        assertThat((commands[1] as StageFiles).files.paths())
                .containsOnly(Paths.get(GradleVersionFile.NAME))
        val commitMessage = (commands[2] as Commit).message
        assertThat(commitMessage.message()).startsWith("Bump version")
        assertThat((commands[3] as PushToRemote).destination.library.repository.remote)
                .isEqualTo(GitHubRepoUrl(orgName, repo))
    }

    private val mockToken = Token("mock_token")
}
