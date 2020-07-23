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
import org.eclipse.jgit.transport.CredentialsProvider

/**
 * A pull request that updates the version of the library and the version of Spine libraries
 * that the project depends on.
 */
class VersionBumpPullRequest(private val library: Library,
                             private val credentials: CredentialsProvider,
                             private val remote: String) {

    private val branchName = BranchName()

    /**
     * Returns the list of commands to execute in order to push a version bump branch to the remote
     * repo.
     */
    fun pushBranch(): List<GitCommand> = listOf(
            CreateBranch(VersionBumpBranch(library, branchName)),
            CommitChanges(VersionBumpCommit(library)),
            PushToRemote(PushMetadata(library, remote, credentials))
    )

    fun createPr() {

    }

    /**
     * Merges this pull request to the `master` remote branch.
     */
    fun merge() {
        // TODO:2020-07-21:serhii.lekariev: implement
    }
}

data class BranchName(val value: String = "bump-version")
