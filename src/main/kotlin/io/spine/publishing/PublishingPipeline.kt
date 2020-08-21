package io.spine.publishing

import io.spine.publishing.git.GitHubToken
import io.spine.publishing.operation.*

/**
 * A series of operations to perform over a set of libraries in order to update them and
 * publish new versions.
 *
 * To publish libraries, a set of predefined operations is performed.
 *
 * 1) Libraries are ensured to match their [remote versions][SetToCurrentRemote].
 * 2) The versions are [changed locally][UpdateVersions].
 * 3) The libraries are built to [verify][EnsureBuilds] that the version updated went correctly.
 * 4) The updated libraries are [published][Publish] to the remote artifact library.
 * 5) The version changes are [propagated][UpdateRemote] to the libraries' remote repositories.
 *
 * @param libraries libraries to update and publish
 * @param operations operations to perform over the libraries in order to update and publish them
 */
class PublishingPipeline(val libraries: LibrariesToPublish,
                         private val operations: List<PipelineOperation>) {

    /**
     * Constructs a pipeline that publishes the library.
     *
     * See class level documentation for the exact steps.
     *
     * @param libraries libraries to update and publish
     * @param token a token that authorizes GitHub operations
     */
    constructor(libraries: LibrariesToPublish, token: GitHubToken) :
            this(libraries, listOf(
                    SetToCurrentRemote(),
                    UpdateVersions(),
                    EnsureBuilds(),
                    Publish(),
                    UpdateRemote(token)
            ))

    /**
     * Passes the libraries through all of the operations, terminating early on the first [Error].
     *
     * Each operation may [error out][Error], in which case the execution is stopped
     * and the error is returned.
     *
     * If every operation finishes successfully, [Ok] is returned.
     */
    fun eval(): OperationResult {
        info().log("Starting the pipeline over the libraries: " +
                "`${libraries.toSet().map { it.name }}`.")
        var pipelineResult: OperationResult = Ok
        for (operation in operations) {
            pipelineResult = operation.doPerform(libraries)
            if (pipelineResult is Error) {
                return logAndReturn(pipelineResult)
            }
        }
        return pipelineResult
    }

    private fun logAndReturn(pipelineResult: Error): OperationResult {
        System.err.println("An error occurred when performing an operation `${javaClass.name}`.")
        System.err.println(pipelineResult.description)
        pipelineResult.exception?.printStackTrace(System.err)
        return pipelineResult
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
     *
     * @param libraries the libraries participating in the publishing pipeline
     */
    abstract fun perform(libraries: LibrariesToPublish): OperationResult

    /**
     * Tries to perform this operation over a set of libraries.
     *
     * If it finishes normally, returns the result of [perform]. If an exception is thrown,
     * returns an [Error].
     *
     * @param libraries the libraries participating in the publishing pipeline
     */
    fun doPerform(libraries: LibrariesToPublish): OperationResult {
        return try {
            perform(libraries)
        } catch (e: Exception) {
            Error("An unexpected error occurred when performing operation `$javaClass`.",
                    e)
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
 *
 * @param description a human-readable description of the error
 * @param exception an exception that has led to the error
 */
class Error(val description: String, val exception: Exception?) : OperationResult() {

    constructor(description: String) : this(description, null)
}
