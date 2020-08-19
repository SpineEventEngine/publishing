package io.spine.publishing.given

import io.spine.publishing.*
import io.spine.publishing.git.GitHubRepoUrl
import io.spine.publishing.git.GitRepository
import java.nio.file.Paths

/**
 * Utilities for testing the [io.spine.publishing.PublishingPipeline]
 */
object PipelineTestEnv {

    val sampleRemote = GitHubRepoUrl("sample_org", "sample_library")

    // The paths is not required for tests, so a mock path is OK.
    val sampleLibrary =
            Library("sample_library",
                    listOf(),
                    GitRepository(Paths.get(""), sampleRemote),
                    Artifact(GroupId("hk", "sample"), "library"))

    /**
     * An operation that always throws an exception.
     */
    object ThrowingOperation : PipelineOperation() {
        override fun perform(libraries: LibrariesToPublish): OperationResult =
                throw IllegalStateException()

    }

    /**
     * An operation that always returns an [io.spine.publishing.Error]
     */
    object ErroneousOperation : PipelineOperation() {
        override fun perform(libraries: LibrariesToPublish): OperationResult =
                Error("An erroneous operation always errors.")
    }

    class CollectingOperation : PipelineOperation() {

        private val seenLibraries: MutableList<Library> = mutableListOf()

        override fun perform(libraries: LibrariesToPublish): OperationResult {
            seenLibraries.addAll(libraries.toSet())
            return Ok
        }

        fun seenLibraries(): List<Library> = seenLibraries.toList()
    }
}
