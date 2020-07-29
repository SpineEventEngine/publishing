package io.spine.publishing.operations

import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.gradle.GradleProject
import io.spine.publishing.gradle.Library

class Publish: PipelineOperation {

    override fun perform(libraries: Set<Library>): OperationResult {
        libraries.map { GradleProject(it.rootDir) }
                .forEach { it.publish() }
        return Ok(libraries)
    }
}
