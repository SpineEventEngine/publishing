package io.spine.publishing

import io.spine.publishing.git.Token
import io.spine.publishing.gradle.Library
import io.spine.publishing.operation.*

/**
 * A series of operations to perform over a set of libraries in order to update them and
 * publish new versions.
 *
 * @param libraries libraries to update and publish
 * @param operations operations to perform over the libraries in order to update and publish them
 */
class PublishingPipeline(val libraries: Set<Library>,
                         private val operations: List<PipelineOperation>) {

    /**
     * Constructs a publishing pipeline for the specified libraries using the following operations:
     *
     * 1) [ensure that the libraries match their remote versions][UpdateToRecent];
     * 2) [change their versions locally][UpdateVersions];
     * 3) [verify that the version update went successfully by building the libraries][EnsureBuilds];
     * 4) [publish the updated libraries to a remote artifact repository][Publish];
     * 5) [update the libraries in the respective remote repositories][UpdateRemote]
     */
    constructor(libraries: Set<Library>) :
            this(libraries, listOf(
                    UpdateToRecent(),
                    UpdateVersions(),
                    EnsureBuilds(),
                    Publish(),
                    UpdateRemote(libsToRemotes, Token("").provider())
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
                .map { operationResult(it, libraries) }
                .firstOrNull { it is Error }
                ?: Ok
    }

    private fun operationResult(ops: PipelineOperation, libs: Set<Library>): OperationResult {
        return try {
            ops.perform(libs)
        } catch (e: Exception) {
            Error(e)
        }
    }
}

/**
 * An operation in a pipeline.
 *
 * Either [finishes successfully][Ok], or returns an [error with a description][Error].
 */
interface PipelineOperation {

    fun perform(libraries: Set<Library>): OperationResult
}

sealed class OperationResult

object Ok : OperationResult()

class Error(val exception: Exception?) : OperationResult() {

    constructor() : this(null)
}
