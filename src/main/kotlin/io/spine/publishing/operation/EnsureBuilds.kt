package io.spine.publishing.operation

import io.spine.publishing.Error
import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.gradle.GradleProject
import io.spine.publishing.gradle.Library
import io.spine.publishing.gradle.Ordering

/**
 * Makes sure that each of the libraries can be built.
 *
 * This includes making sure that for every library, its dependencies are built and checked
 * before building the library.
 */
class EnsureBuilds : PipelineOperation {

    companion object {
        private const val CHECK_LOGS = "Check logs for Gradle output."
    }

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
     * @see Ordering
     */
    override fun perform(libraries: Set<Library>): OperationResult {
        val ordered = Ordering(libraries).byDependencies
        for (library in ordered) {
            val gradleProject = GradleProject(library.rootDir)
            val builds = gradleProject.build()
            if (!builds) {
                return Error("Library `${library.name}` does not pass the build. " +
                        CHECK_LOGS)
            }
            val published = gradleProject.publishToMavenLocal()
            if (!published) {
                return Error("Library `${library.name}` could not be published " +
                        "to Maven local. " + CHECK_LOGS)
            }
        }
        return Ok
    }
}
