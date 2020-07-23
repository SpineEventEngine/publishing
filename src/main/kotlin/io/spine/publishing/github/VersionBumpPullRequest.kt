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

import io.spine.publishing.git.*
import org.eclipse.jgit.transport.CredentialsProvider
import org.kohsuke.github.GitHub

/**
 * A pull request that updates the version of the library and the version of Spine libraries
 * that the project depends on.
 */
class VersionBumpPullRequest(private val remote: RemoteRepository,
                             private val credentials: CredentialsProvider) {

    companion object {
        private val branchName = BranchName()
        private const val MASTER = "master"
    }

    /**
     * Returns the list of commands to execute in order to push a version bump branch to the remote
     * repo.
     */
    fun pushBranch(): List<GitCommand> = listOf(
            CreateBranch(VersionBumpBranch(remote.library, branchName)),
            CommitChanges(VersionBumpCommit(remote.library)),
            PushToRemote(PushMetadata(remote, credentials))
    )

    /**
     * Creates a version pumping pull request and merges it.
     *
     * This method requires a necessary branch to already be
     * [pushed][VersionBumpPullRequest.pushBranch] to GitHub.
     */
    fun mergeVersionBump() {
        GitHub.connect()
                .getRepository(remote.gitHubRepository.repoIdentifier())
                .createPullRequest(PrTitle().value,
                        branchName.value,
                        MASTER,
                        PrDescription().value)
                // `null` is fine, see `merge` javadoc.
                .merge(null)
    }

    inner class PrTitle(val value: String = "Update Spine libraries")

    inner class PrDescription(val value: String = "This automatically generated PR brings the " +
            "version of all Spine libraries up to `${this@VersionBumpPullRequest.remote.library
                    .version()}`")
}

data class BranchName(val value: String = "bump-version")
