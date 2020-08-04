package io.spine.publishing.operation

import io.spine.publishing.Error
import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.gradle.GradleProject
import io.spine.publishing.Library
import io.spine.publishing.gradle.Ordering

/**
 * Makes sure that each of the libraries can be built.
 *
 * This includes making sure that for every library, its dependencies are built and checked
 * before building the library.
 */
class EnsureBuilds : PipelineOperation() {

    /**
     * Goes through each library, building them to ensure that the dependencies are consistent.
     *
     * For example:
     *
     *          A --------------> B
     *           \               /
     *            --------> C <--
     * library A depends on library B and C. Library B depends on library C.
     * In such a configuration, the following happens:
     *
     * 1) C is built;
     * 2) C is published to the local Maven repo, so that it can be used for further builds;
     * 3) B is built;
     * 4) B is published to the local Maven repo;
     * 5) A is built;
     * 6) A is published to the local Maven repo;
     *
     * @param libraries a collection of interdependent libraries to check
     *
     * @see Ordering for a dependency-safe way to order libraries
     */
    override fun perform(libraries: Set<Library>): OperationResult {
        val ordered = Ordering(libraries).byDependencies
        for (library in ordered) {
            val gradleProject = GradleProject(library.rootDir)
            val builds = gradleProject.build()
            if (!builds) {
                return Error(cannotBuild(library, libraries))
            }
            val published = gradleProject.publishToMavenLocal()
            if (!published) {
                return Error(cannotPublish(library))
            }
        }
        return Ok
    }

    private fun cannotBuild(library: Library, allLibraries: Set<Library>): String {
        val versions = allLibraries.map { it.version() }
                .joinToString { it.toString() }
        return """Cannot build library `${library.name}`. Versions of all libraries: `$versions`."""
    }

    private fun cannotPublish(library: Library): String =
            """Cannot publish library `${library.name}`. 
                |Check logs for details of the Gradle task.""".trimMargin()

}
