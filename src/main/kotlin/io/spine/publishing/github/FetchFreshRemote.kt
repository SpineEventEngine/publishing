package io.spine.publishing.github

import io.spine.publishing.git.Fetch
import io.spine.publishing.git.GitCommand
import io.spine.publishing.git.Reset
import io.spine.publishing.git.ToOriginMaster
import io.spine.publishing.gradle.Library

/**
 * Returns the commands that, when executed, set the Git repository associated with the specified
 * library to the current state of its remote `master` branch.
 *
 * @param library library to set to its remote `master` state
 */
fun fetchFresh(library: Library): List<GitCommand> = listOf(
        Fetch(library),
        Reset(ToOriginMaster(library))
)
