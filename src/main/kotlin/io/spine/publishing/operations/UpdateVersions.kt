package io.spine.publishing.operations

import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.gradle.Library

class UpdateVersions : PipelineOperation {

    override fun perform(libraries: Set<Library>): OperationResult {
        // The collection is non-empty as per the pipeline contract - non-null assertion is safe.
        val maxVersion = libraries.maxBy { it.version() }!!.version()
        libraries.forEach { library ->
            library.update(maxVersion)
        }

        return Ok(libraries)
    }
}
