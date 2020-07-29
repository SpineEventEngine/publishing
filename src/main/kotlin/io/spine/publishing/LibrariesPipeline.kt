package io.spine.publishing

import io.spine.publishing.gradle.Library

class LibrariesPipeline(val libraries: Set<Library>, val operations: List<PipelineOperation>) {

    fun eval(): OperationResult {
        var libraries = libraries

        for (operation in operations) {
            val result = operationResult(operation, libraries)
            if (result is Error) {
                return result
            } else {
                libraries = (result as Ok).libraries
            }
        }

        return Ok(libraries)
    }

    private fun operationResult(ops: PipelineOperation, libs: Set<Library>): OperationResult {
        return try {
            ops.perform(libs)
        } catch (e: Exception) {
            Error("A pipeline operation has errored unexpectedly.", e)
        }
    }
}

interface PipelineOperation {

    fun perform(libraries: Set<Library>): OperationResult
}

sealed class OperationResult

class Ok(val libraries: Set<Library>) : OperationResult()

class Error(val description: String, e: Exception?) : OperationResult()

