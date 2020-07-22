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

import io.spine.publishing.gradle.LibraryName
import io.spine.publishing.gradle.Version

/**
 * A pull request that updates the version of the library and the version of Spine libraries
 * that the project depends on.
 */
class VersionBumpPullRequest(private val branchName: BranchName,
                             private val libraryName: LibraryName,
                             private val newVersion: Version) {

    override fun toString(): String {
        return """Bump version to `$newVersion`"""
    }

    /**
     * Creates this pull request. The branch must already be present in the
     * remote repository.
     */
    fun create() {
        // TODO:2020-07-21:serhii.lekariev: implement
    }

    /**
     * Merges this pull request to the `master` remote branch.
     */
    fun merge() {
        // TODO:2020-07-21:serhii.lekariev: implement
    }
}
