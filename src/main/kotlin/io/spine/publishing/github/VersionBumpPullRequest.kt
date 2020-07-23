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

import io.spine.publishing.gradle.Library
import io.spine.publishing.gradle.LibraryName
import io.spine.publishing.gradle.Version
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

/**
 * A pull request that updates the version of the library and the version of Spine libraries
 * that the project depends on.
 */
class VersionBumpPullRequest(private val library: Library) {

    override fun toString(): String {
        return """Bump version to `${library.version()}`"""
    }

    /**
     * Creates this pull request. The branch must already be present in the
     * remote repository.
     */
    fun create() {
        // Checks out the new branch, commits the version file <- knows about it, since its a version bump
        // Pushes the branch
        // -- local part is done --
        // Creates a PR
    }

    private fun newBranch(name: BranchName) {
        val repo = FileRepositoryBuilder()
                .setWorkTree(gradleProject.rootDir.toFile())
                .readEnvironment()
                .findGitDir()
                .build()

        val git = Git(repo)
        val branch = git
                .checkout()
                .setCreateBranch(true)
                .setName(name.value)
                .call()

        val revCommit = git.commit()

                .setMessage("bump")
                .call()
        // 80f789dcd6f7ce863ee4dea4edd8b83b7803c032
        git.push().setCredentialsProvider(UsernamePasswordCredentialsProvider("80f789dcd6f7ce863ee4dea4edd8b83b7803c032", "")).setRemote("https://github.com/SpineEventEngine/publishing.git").call()
        git.checkout().setName("github").call()
        git.branchDelete().setBranchNames(name.value)
    }

    /**
     * Merges this pull request to the `master` remote branch.
     */
    fun merge() {
        // TODO:2020-07-21:serhii.lekariev: implement
    }
}
