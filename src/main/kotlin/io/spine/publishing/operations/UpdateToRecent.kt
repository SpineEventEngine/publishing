package io.spine.publishing.operations

import io.spine.publishing.Error
import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.git.Git
import io.spine.publishing.github.FetchFreshRemote
import io.spine.publishing.gradle.Library

/**
 * Sets the state of the libraries to the current state of their remote GitHub counterpart.
 *
 * @see FetchFreshRemote
 */
class UpdateToRecent : PipelineOperation {

    override fun perform(libraries: Set<Library>): OperationResult {
        return try {
            libraries.map { FetchFreshRemote(it).fetchFresh() }
                    .forEach { Git.executeAll(it) }
            Ok
        } catch (e: Exception) {
            Error("Could not update fetch recent library versions: `$libraries`.", e)
        }
    }
}
