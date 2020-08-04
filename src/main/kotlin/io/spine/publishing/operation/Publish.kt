package io.spine.publishing.operation

import io.spine.publishing.Library
import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.gradle.GradleProject

/**
 * Publishes the libraries to the remote artifact repository.
 *
 * The artifact repository to use is defined by the libraries `publish` task.
 */
class Publish : PipelineOperation() {

    override fun perform(libraries: Set<Library>): OperationResult {
        libraries.map { GradleProject(it.repository.localRootPath) }
                .forEach { it.publish() }
        return Ok
    }
}
