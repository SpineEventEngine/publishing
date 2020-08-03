package io.spine.publishing.operation

import io.spine.publishing.Error
import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.github.fetchFresh
import io.spine.publishing.gradle.Library

/**
 * Sets the state of the libraries to the current state of their remote GitHub counterpart.
 *
 * @see fetchFresh
 */
class UpdateToRecent : PipelineOperation {

    override fun perform(libraries: Set<Library>): OperationResult {
        return try {
            libraries.flatMap { fetchFresh(it) }
                    .forEach { it.execute() }
            Ok
        } catch (e: Exception) {
            Error(e)
        }
    }
}
