package io.spine.publishing.github

import io.spine.publishing.gradle.LibraryName
import io.spine.publishing.gradle.Version

/**
 * A pull request that updates the version of the library and the version of Spine libraries that the project depends
 * on.
 */
class VersionBumpPullRequest(private val branchName: BranchName,
                             private val libraryName: LibraryName,
                             private val newVersion: Version) {

    override fun toString(): String {
        return """Bump version to `$newVersion`"""
    }

    /**
     * Creates this pull request. The branch must already be present in the remote repository.
     */
    fun create() {
        // TODO:2020-07-21:serhii.lekariev: implement
    }

    fun merge() {
        // TODO:2020-07-21:serhii.lekariev: implement
    }
}