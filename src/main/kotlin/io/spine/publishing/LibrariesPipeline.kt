package io.spine.publishing

import io.spine.publishing.gradle.Library

/**
 * A series of operations performed over a set of libraries.
 *
 * Each operation may [error out][Error], in which case the execution is stopped and the error
 * is returned.
 *
 * If every operation finishes successfully, [Ok] is returned.
 */
class LibrariesPipeline(val libraries: Set<Library>, val operations: List<PipelineOperation>) {

    /**
     * Passes the libraries through all of the operations, terminating early on the first [Error].
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
            Error("A pipeline operation has errored unexpectedly.", e)
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

class Error(val description: String, e: Exception?) : OperationResult() {

    constructor(description: String) : this(description, null)
}

