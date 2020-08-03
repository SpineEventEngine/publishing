package io.spine.publishing.operation

import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.gradle.Library
import io.spine.publishing.gradle.Version

/**
 * Updates the `version.gradle.kts` files for every library. As a result, every library will have
 * its version and the version of its dependencies set to the maximum version among the specified
 * dependencies.
 */
class UpdateVersions : PipelineOperation {

    override fun perform(libraries: Set<Library>): OperationResult {
        // The collection is non-empty as per the pipeline contract - non-null assertion is safe.
        val maxVersion = libraries.maxBy { it.version() }!!.version()
        libraries
                .filter { needsUpdate(it, maxVersion) }
                .forEach { library ->
            library.update(maxVersion)
        }

        return Ok
    }

    private fun needsUpdate(library: Library, newVersion: Version) =
            needsOwnUpdate(library, newVersion) || needsDependencyUpdate(library, newVersion)

    private fun needsOwnUpdate(library: Library, newVersion: Version) =
            library.version() < newVersion

    private fun needsDependencyUpdate(library: Library, newVersion: Version) =
            library.versionFile.declaredDependencies()
                    .any { it.value < newVersion }
}
