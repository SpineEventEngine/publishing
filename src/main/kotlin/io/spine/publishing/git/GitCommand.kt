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
 * Git commands necessary for the `Publishing` application.
 */
sealed class GitCommand(payload: GitCommandPayload) {

    val repository: Repository = payload.repository()
}

class Add(val files: FilesToAdd) : GitCommand(files)

/**
 * Checks out a local branch.
 *
 * Does not create a new branch.
 */
class Checkout(val checkout: CheckoutBranch) : GitCommand(checkout)

/**
 * Commits changed files to the current branch.
 *
 * This command adds the files before committing them. Every file is added entirely.
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

    /**
     * Executes the specified Git commands 1 by 1.
     */
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
            is Add -> {
                val add = git.add()
                command.files.files().forEach { add.addFilepattern(it.toString()) }
                add.call()
            }

            is Checkout -> git.checkout()
                    .setCreateBranch(false)
                    .setName(command.checkout.name())
                    .call()

            is CommitChanges -> {
                val commitBuilder = git.commit().setMessage(command.commit.message())
                command.commit.files().forEach { commitBuilder.setOnly(it.toString()) }
                commitBuilder.call()
            }

            is PushToRemote -> git
                    .push()
                    .setRemote(command.pushMetadata.remote.gitHubRepository.asUrl())
                    .setCredentialsProvider(command.pushMetadata.credentials)
                    .call()
        }
    }
}
