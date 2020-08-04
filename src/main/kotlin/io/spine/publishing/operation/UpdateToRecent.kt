package io.spine.publishing.operation

import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.git.Fetch
import io.spine.publishing.git.GitCommand
import io.spine.publishing.git.Reset
import io.spine.publishing.git.ToOriginMaster
import io.spine.publishing.gradle.Library

/**
 * Sets the state of the libraries to the current state of their remote GitHub counterpart.
 *
 * @see fetchFresh
 */
class UpdateToRecent : PipelineOperation() {

    override fun perform(libraries: Set<Library>): OperationResult {
        libraries.flatMap { fetchFresh(it) }
                .forEach { it.execute() }
        return Ok
    }

    /**
     * Returns the commands that, when executed, set the Git repository associated with the
     * specified library to the current state of its remote `master` branch.
     *
     * @param library library to set to its remote `master` state
     */
    private fun fetchFresh(library: Library): List<GitCommand> = listOf(
            Fetch(library),
            Reset(ToOriginMaster(library))
    )
}
