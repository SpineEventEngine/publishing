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

import org.eclipse.jgit.api.CommitCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryBuilder
import java.nio.file.Path

class Branch(workingDir: Path, private val name: BranchName) {

    private val repository = RepositoryBuilder()
            .readEnvironment()
            .setWorkTree(workingDir.toAbsolutePath().toFile())
            .build()

    private val git = Git(repository)

    init {
        git.checkout()
           .setCreateBranch(true)
           .setName(name.value)
           .call()
    }

    fun addCommit(commit: Commit) {
        val commitBuilder = git.commit()
                .setMessage(commit.commitMessage)
        addAllPaths(commit.trackedFiles, commitBuilder)

        commitBuilder.call()
    }

    fun push() {
        git.push()
                .set
    }

    private fun addAllPaths(paths: Iterable<Path>, commit: CommitCommand) {
        paths.map { it.toAbsolutePath() }
                .map { it.toString() }
                .forEach { commit.setOnly(it) }
    }


}
