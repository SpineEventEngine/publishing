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
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isNotNull
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("`GitCommands` should")
class GitCommandsTest {

    private val sampleFileContents = """This is a sample file for Git related tests."""
    private val sampleFileName = "test.txt"

    private lateinit var repoPath: Path
    private lateinit var repository: Repository
    private lateinit var sampleFile: Path

    @BeforeEach
    fun setupRepo() {
        repoPath = Files.createTempDirectory("io.spine.publishing.git_commands_test")
        sampleFile = repoPath.resolve(sampleFileName)
        Git.init().setDirectory(repoPath.toFile()).call()
        sampleFile.toFile().createNewFile()
        sampleFile.toFile().printWriter().use {
            it.println(sampleFileContents)
            it.println()
        }
        repository = Git.open(repoPath.toFile()).repository
        Git(repository).add().addFilepattern(".")
                .call()
        Git(repository).commit().setMessage("Add the sample file.").setAll(true).call()
    }

    @Test
    @DisplayName("create a new branch")
    fun createBranch() {
        val gitRepo = Git.open(repoPath.toFile())

        val createBranch = CreateBranch(object : Branch {
            override fun name(): BranchName = "test"
            override fun repository(): Repository = gitRepo.repository
        })

        io.spine.publishing.git.Git.execute(createBranch)

        val branches = Git(repository).branchList().call()
        assertThat(branches).hasSize(2) // `master` and the newly created one.
        assertThat(branches.map { it.name }.find { it.contains("test") }).isNotNull()
    }

    @Test
    @DisplayName("create a new commit")
    fun createCommit() {
        val gitRepo = Git.open(repoPath.toFile())
        sampleFile.toFile().printWriter().use {
            it.println()
            it.println("a fresh new line")
            it.println()
        }
        val commitMessage = "A sample change"
        val commitChanges = CommitChanges(object : Commit {
            override fun message(): CommitMessage = commitMessage
            override fun file(): Path = repoPath.relativize(sampleFile)
            override fun repository(): Repository = gitRepo.repository
        })

        io.spine.publishing.git.Git.execute(commitChanges)

        val branches = Git(repository).branchList().call()
        // `master` only.
        assertThat(branches).hasSize(1)
        val refs = Git(repository).log().call().toList()
        // Initial commit and a newly created one.
        assertThat(refs).hasSize(2)
        // `log` outputs are FIFO - the freshest ones are at the top.
        val freshCommit = refs.toList()[0]
        assertThat(freshCommit.fullMessage).contains(commitMessage)
    }
}
