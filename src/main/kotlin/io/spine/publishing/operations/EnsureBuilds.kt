package io.spine.publishing.operations

import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.gradle.GradleProject
import io.spine.publishing.gradle.Library
import io.spine.publishing.gradle.Ordering

class EnsureBuilds : PipelineOperation {

    override fun perform(libraries: Set<Library>): OperationResult {
        val ordered = Ordering(libraries).byDependencies
        for (library in ordered) {
            val gradleProject = GradleProject(library.rootDir)
            gradleProject.build()
            gradleProject.publishToMavenLocal()
        }
        return Ok(libraries)
    }
}
