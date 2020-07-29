package io.spine.publishing.github

import io.spine.publishing.git.Fetch
import io.spine.publishing.git.GitCommand
import io.spine.publishing.git.Reset
import io.spine.publishing.git.ResetHardOriginMaster
import io.spine.publishing.gradle.Library

/**
 * Sets the Git repository associated with the specified library to the current state of its
 * remote `master` branch.
 */
class FetchFreshVersion(private val library: Library) {

    fun fetchFresh(): List<GitCommand> = listOf(
            Fetch(library),
            Reset(ResetHardOriginMaster(library))
    )
}
