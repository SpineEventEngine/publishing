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

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository

/**
 * Git commands necessary for the publishing applications.
 */
sealed class GitCommand(payload: GitCommandPayload) {

    val repository: Repository = payload.repository()
}

/**
 * Checks out a new branch in the local repository.
 */
class CreateBranch(val branch: Branch) : GitCommand(branch)

/**
 * Commits changed files to the current branch.
 *
 * Note that this command cannot commit a file partially. The [entire file][VersionBumpCommit.file]
 * is committed.
 */
class CommitChanges(val commit: Commit) : GitCommand(commit)

/**
 * Pushes a current local branch to the remote repository.
 */
class PushToRemote(val pushMetadata: PushMetadata) : GitCommand(pushMetadata)

/**
 * Executes Git commands.
 */
object Git {

    fun executeAll(commands: List<GitCommand>) {
        commands.forEach { execute(it) }
    }

    /**
     * Executes the specified Git command.
     *
     * This method causes changes in the underlying local repositories.
     */
    fun execute(command: GitCommand) {
        val git = Git(command.repository)
        when (command) {
            is CreateBranch -> git
                    .checkout()
                    .setCreateBranch(true)
                    .setName(command.branch.name())
                    .call()

            is CommitChanges -> git
                    .commit()
                    .setOnly(command.commit.file().toString())
                    .setMessage(command.commit.message())
                    .call()

            is PushToRemote -> git
                    .push()
                    .setRemote(command.pushMetadata.remote.gitHubRepository.asUrl())
                    .setCredentialsProvider(command.pushMetadata.credentials)
                    .call()
        }
    }
}
