package io.spine.publishing.operation

import io.spine.publishing.*
import io.spine.publishing.gradle.GradleProject

/**
 * Publishes the libraries to the remote artifact repository.
 *
 * The artifact repository to use is defined by the libraries `publish` task.
 */
class Publish : PipelineOperation() {

    override fun perform(libraries: LibrariesToPublish): OperationResult {
        libraries
                .toSet()
                .filter { !SpineCloudRepoArtifact(it.artifact).isPublished(it.version()) }
                .map { GradleProject(it.repository.localRootPath) }
                .forEach { it.publish() }
        return Ok
    }
}
