package io.spine.publishing

import io.spine.publishing.git.Token
import io.spine.publishing.gradle.Library
import io.spine.publishing.operation.*

/**
 * A series of operations to perform over a set of libraries in order to update them and
 * publish new versions.
 *
 * To publish libraries, a set of predefined operations is performed.
 *
 * 1) [Libraries are ensured to match their remote versions][UpdateToRecent].
 * 2) [The versions are changed locally][UpdateVersions].
 * 3) [The libraries are built to verify that the version updated went correctly][EnsureBuilds].
 * 4) [The updated libraries are published to the remote artifact library][Publish].
 * 5) [The version changes are propagated to the libraries' remote repositories][UpdateRemote].
 *
 * @param libraries libraries to update and publish
 * @param operations operations to perform over the libraries in order to update and publish them
 */
class PublishingPipeline(val libraries: Set<Library>,
                         private val operations: List<PipelineOperation>) {

    companion object {
        private fun toLocalLibs(libraries: Set<LibraryToUpdate>) =
                libraries.map { it.local }
                        .toSet()
    }

    /**
     * Constructs a pipeline that publishes the library.
     *
     * See class level documentation for the exact steps.
     *
     * @param libraries libraries to update and publish
     */
    constructor(libraries: Set<LibraryToUpdate>) :
            this(toLocalLibs(libraries), listOf(
                    UpdateToRecent(),
                    UpdateVersions(),
                    EnsureBuilds(),
                    Publish(),
                    UpdateRemote(libraries.toList(), Token("").provider())
            ))

    /**
     * Passes the libraries through all of the operations, terminating early on the first [Error].
     *
     * Each operation may [error out][Error], in which case the execution is stopped and the error
     * is returned.
     *
     * If every operation finishes successfully, [Ok] is returned.
     */
    fun eval(): OperationResult {
        return operations
                .map { it.doPerform(libraries) }
                .firstOrNull { it is Error }
                ?: Ok
    }
}

/**
 * An operation in a pipeline.
 *
 * Either [finishes successfully][Ok], or returns an [error with a description][Error].
 */
abstract class PipelineOperation {

    /**
     * Perform this operation over the specified set of libraries.
     *
     * Returns [Ok] if the operation has finished successfully, and [Error] otherwise.
     */
    abstract fun perform(libraries: Set<Library>): OperationResult

    /**
     * Tries to perform this operation over a set of libraries.
     *
     * If it finishes normally, returns the result of [perform]. If an exception is thrown,
     * returns an [Error].
     */
    fun doPerform(libraries: Set<Library>): OperationResult {
        return try {
            perform(libraries)
        } catch (e: Exception) {
            Error(e)
        }
    }
}

/**
 * A result of a pipeline operation.
 *
 * An operation can either finish [successfully][Ok] or with an [Error].
 */
sealed class OperationResult

/**
 * Signifies that the pipeline operation has finished successfully.
 */
object Ok : OperationResult()

/**
 * Signifies that the pipeline operation has finished with an error.
 */
class Error(val exception: Exception?) : OperationResult() {

    constructor() : this(null)
}
