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

import io.spine.publishing.Library
import io.spine.publishing.github.RetryPolicy
import io.spine.publishing.github.TokenFactory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType.HARD
import org.eclipse.jgit.lib.Repository

/**
 * A configurable Git command.
 *
 * Wraps around an [Eclipse JGit][Git] object to provide the commands necessary for
 * the Publishing application.
 *
 * @param options options that customize the behavior of this command
 */
sealed class GitCommand(options: GitCommandOptions) {

    /**
     * A repository associated with a Git command.
     */
    val repository: Repository = options.repository()

    /**
     * Executes the command.
     *
     * May throw an exception if an error occurs.
     */
    abstract fun execute()

    /**
     * Returns the `Git` API-object that eases the construction of commands.
     *
     * Call only from extenders.
     */
    internal fun git() = Git(repository)
}

/**
 * Resets the current branch to the state specified by the [ResetTarget].
 *
 * @param target specifies the behavior of the reset command
 */
class Reset(private val target: ResetTarget) : GitCommand(target) {

    override fun execute() {
        val command = git().reset()
                .setRef(target.ref())
        if (target.isHard()) {
            command.setMode(HARD)
        }
        command.call()
    }
}

/**
 * Fetches the updates from the remote repo.
 *
 * Note that no remote is given to this command, as the default `origin` is sufficient.
 *
 * @param library the library to fetch the remote master version of
 */
class Fetch(library: Library) : GitCommand(object : GitCommandOptions {
    override fun repository() = library.repository.localGitRepository()
}) {

    override fun execute() {
        git().fetch()
                .call()
    }
}

/**
 * Stages the files for commit.
 *
 * Note that only the staged files can be [committed][Commit].
 *
 * @param files specifies which files are staged for commit
 */
class StageFiles(val files: FilesToStage) : GitCommand(files) {

    override fun execute() {
        val add = git().add()
        files.paths()
                .forEach { add.addFilepattern(it.toString()) }
        add.call()
    }
}

/**
 * Checks out a local branch.
 *
 * Does not create a new branch, therefore the branch should exist.
 *
 * @param branch specifies the branch to checkout
 */
class Checkout(val branch: Branch) : GitCommand(branch) {

    override fun execute() {
        git().checkout()
                .setCreateBranch(false)
                .setName(branch.name())
                .call()
    }
}

/**
 * Commits the [staged files][StageFiles] to the current branch.
 *
 * @param message the message that is associated with the commit
 */
class Commit(val message: CommitMessage) : GitCommand(message) {

    override fun execute() {
        git().commit()
                .setMessage(message.message())
                .call()
    }
}

/**
 * Pushes the current local branch to the remote repository.
 *
 * @param gitRepo the repository that the push is performed on
 * @param tokenFactory factory of tokens to authorize requests
 */
class PushToRemote(val gitRepo: GitRepository,
                   private val tokenFactory: TokenFactory,
                   private val retryPolicy: NoExpiredTokens =
                           NoExpiredTokens(gitRepo.remote, 3, tokenFactory)) :
        GitCommand(object : GitCommandOptions {
            override fun repository() = gitRepo.localGitRepository()
        }) {

    override fun execute() {
        val token = retryPolicy.retryUntilOk()
        push(gitRepo, token)
    }
}

/**
 * A retry policy that ensures that only non-expired tokens are used.
 *
 * @param retries the amount of attempts to generate a new token
 * @param remote a remote repository that the generated tokens authorize access to
 * @param tokenFactory a factory of tokens
 */
class NoExpiredTokens(private val remote: GitHubRepoUrl,
                      retries: Int,
                      private val tokenFactory: TokenFactory) :
        RetryPolicy<GitHubToken>(retries) {

    override fun action(): GitHubToken = tokenFactory.newToken()

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun resultOk(token: GitHubToken): Boolean = !token.isExpired

    override fun onRetriesExhausted(): Nothing {
        val message = "Could not push to `$remote` as the token has expired."
        throw IllegalStateException(message)
    }
}

private fun push(gitRepo: GitRepository, token: GitHubToken) {
    Git(gitRepo.localGitRepository())
            .push()
            .setRemote(gitRepo.remote.value(token))
            .call()
}
