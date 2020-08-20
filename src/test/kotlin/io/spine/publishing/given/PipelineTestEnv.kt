package io.spine.publishing.given

import com.google.common.io.Files
import io.spine.publishing.*
import io.spine.publishing.git.GitHubRepoUrl
import io.spine.publishing.git.GitRepository
import java.nio.file.Path

/**
 * Utilities for testing the [io.spine.publishing.PublishingPipeline]
 */
object PipelineTestEnv {

    val sampleRemote = GitHubRepoUrl("sample_org", "sample_library")

    private lateinit var versionFile: Path

    private fun copyResourceFile() {
        val tempDir = Files.createTempDir()
        versionFile = tempDir.toPath().resolve("version.gradle.kts")
        versionFile.toFile().createNewFile()
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val text = PipelineTestEnv::javaClass.get().classLoader.getResource("version.gradle.kts").readText()
        versionFile.toFile().writeText(text)
    }

    fun sampleLibrary(): Library {
        if (!this::versionFile.isInitialized) {
            copyResourceFile()
        }
        return Library("base",
                listOf(),
                GitRepository(versionFile.parent, sampleRemote),
                Artifact(GroupId("hk", "sample"), "library"))
    }

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
