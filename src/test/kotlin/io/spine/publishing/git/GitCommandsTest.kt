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
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.TreeWalk
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
        Git(repository)
                .add()
                .addFilepattern(".")
                .call()
        Git(repository)
                .commit()
                .setMessage("Add the sample file.")
                .setAll(true)
                .call()
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
        val commitChanges = Commit(object : CommitMessage {
            override fun message(): String = commitMessage
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

    @Test
    @DisplayName("create a commit with several files")
    fun multiFileCommit() {
        val gitRepo = Git.open(repoPath.toFile())

        val secondFile = repoPath.resolve("second_file.txt")
        secondFile.toFile().createNewFile()
        secondFile.toFile().printWriter().use {
            it.println("a new line in the second file")
        }
        sampleFile.toFile().printWriter().use {
            it.println("a new line in the first file")
        }

        val stageSecondFile = StageFiles(object : FilesToStage {
            override fun files(): Set<Path> = setOf(repoPath.relativize(sampleFile),
                    repoPath.relativize(secondFile))

            override fun repository(): Repository = gitRepo.repository
        })

        io.spine.publishing.git.Git.execute(stageSecondFile)

        val commit = Commit(object : CommitMessage {
            override fun message(): String = "A change with two files."
            override fun repository(): Repository = gitRepo.repository
        })

        io.spine.publishing.git.Git.execute(commit)

        val commitTree = gitRepo.log().all().call().toList()[0].tree
        val treeWalk = TreeWalk(gitRepo.repository)
        treeWalk.reset(commitTree)
        val files: MutableList<String> = mutableListOf()
        while (treeWalk.next()) {
            files.add(treeWalk.pathString)
        }

        assertThat(files).containsOnly(secondFile.toFile().name, sampleFile.toFile().name)
    }

    @Test
    @DisplayName("reset to a commit")
    fun resetHard() {
        val gitRepo = Git.open(repoPath.toFile())
        sampleFile.toFile().printWriter().use {
            it.println()
            it.println("a fresh new line")
            it.println()
        }
        val commitMessage = "A sample change"
        val commitChanges = Commit(object : CommitMessage {
            override fun message(): String = commitMessage
            override fun repository(): Repository = gitRepo.repository
        })
        io.spine.publishing.git.Git.execute(commitChanges)

        val allCommits = gitRepo.log().all().call().toList()
        assertThat(allCommits).hasSize(2)
        val firstCommit = allCommits[1]
        val reset = Reset(object: ResetTarget {
            override fun ref(): String = firstCommit.name.toString()
            override fun isHard(): Boolean = true
            override fun repository(): Repository = gitRepo.repository
        })
        io.spine.publishing.git.Git.execute(reset)
        val commitsAfterReset = gitRepo.log().all().call().toList()
        assertThat(commitsAfterReset).hasSize(1)
    }
}
