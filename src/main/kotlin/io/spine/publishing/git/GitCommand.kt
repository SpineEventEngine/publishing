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

import io.spine.publishing.gradle.Library
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType.HARD
import org.eclipse.jgit.lib.Repository

/**
 * Git commands necessary for the Publishing application.
 */
sealed class GitCommand(options: GitCommandOptions) {

    val repository: Repository = options.repository()
}

/**
 * Resets the current branch to the state specified by the [ResetTarget].
 */
class Reset(val reset: ResetTarget) : GitCommand(reset)

/**
 * Fetches the update from the remote repo.
 *
 * Note that no remote is given to this command, as the default `origin` is sufficient.
 */
class Fetch(library: Library) : GitCommand(object : GitCommandOptions {
    override fun repository() = library.repository()
})

/**
 * Stages the files for commit.
 *
 * Note that only the staged files can be [committed][Commit].
 */
class StageFiles(val files: FilesToStage) : GitCommand(files)

/**
 * Checks out a local branch.
 *
 * Does not create a new branch, therefore the branch should exist.
 */
class Checkout(val branch: Branch) : GitCommand(branch)

/**
 * Commits the [staged files][StageFiles] to the current branch.
 */
class Commit(val message: CommitMessage) : GitCommand(message)

/**
 * Pushes the current local branch to the remote repository.
 */
class PushToRemote(val destination: PushDestination) : GitCommand(destination)

/**
 * Executes Git commands.
 */
object Git {

    /**
     * Executes the specified Git commands, equivalent to `commands.forEach { Git.exeute(it) }`.
     */
    fun executeAll(commands: List<GitCommand>) = commands.forEach { execute(it) }

    /**
     * Executes the specified Git command.
     *
     * This method causes changes in the underlying local repositories.
     */
    fun execute(command: GitCommand) {
        val git = Git(command.repository)
        when (command) {
            is Fetch -> git.fetch()
                    .call()

            is Reset -> {
                val reset = git.reset()
                        .setRef(command.reset.ref())
                if (command.reset.isHard()) {
                    reset.setMode(HARD)
                }
                reset.call()
            }

            is StageFiles -> {
                val add = git.add()
                command.files.files().forEach { add.addFilepattern(it.toString()) }
                add.call()
            }

            is Checkout -> git.checkout()
                    .setCreateBranch(false)
                    .setName(command.branch.name())
                    .call()

            is Commit -> git.commit()
                    .setMessage(command.message.message())
                    .call()

            is PushToRemote -> git.push()
                    .setRemote(command.destination.remote.gitHubRepository.asUrl())
                    .setCredentialsProvider(command.destination.credentials)
                    .call()
        }
    }
}
