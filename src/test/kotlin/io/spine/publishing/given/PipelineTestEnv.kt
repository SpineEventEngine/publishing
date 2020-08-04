package io.spine.publishing.given

import io.spine.publishing.Error
import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.gradle.Library
import java.nio.file.Paths

/**
 * Utilities for testing the [io.spine.publishing.PublishingPipeline]
 */
object PipelineTestEnv {

    // The paths is not required for tests, so a mock path is OK.
    val sampleLibrary = Library("sample_library", listOf(), Paths.get(""))

    /**
     * An operation that always throws an exception.
     */
    object ThrowingOperation : PipelineOperation() {
        override fun perform(libraries: Set<Library>): OperationResult =
                throw IllegalStateException()

    }

    /**
     * An operation that always returns an [io.spine.publishing.Error]
     */
    object ErroneousOperation : PipelineOperation() {
        override fun perform(libraries: Set<Library>): OperationResult =
                Error("An erroneous operation always errors.")
    }

    class CollectingOperation : PipelineOperation() {

        private val seenLibraries: MutableList<Library> = mutableListOf()

        override fun perform(libraries: Set<Library>): OperationResult {
            seenLibraries.addAll(libraries)
            return Ok
        }

        fun seenLibraries(): List<Library> = seenLibraries.toList()
    }
}
