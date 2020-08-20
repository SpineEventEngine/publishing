package io.spine.publishing.operation

import io.spine.publishing.*
import io.spine.publishing.git.Fetch
import io.spine.publishing.git.GitCommand
import io.spine.publishing.git.Reset
import io.spine.publishing.git.ToOriginMaster

/**
 * Sets the state of the libraries to the current state of their remote GitHub counterpart.
 */
class SetToCurrentRemote : PipelineOperation() {

    override fun perform(libraries: LibrariesToPublish): OperationResult {
        libraries.toSet()
                .flatMap { fetchFresh(it) }
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
