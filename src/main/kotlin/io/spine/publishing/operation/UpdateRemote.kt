package io.spine.publishing.operation

import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.LibraryToUpdate
import io.spine.publishing.github.updateVersion
import io.spine.publishing.gradle.Library
import org.eclipse.jgit.transport.CredentialsProvider

/**
 * Updates the remote library repositories.
 *
 * This operation assumes that the libraries have initialised Git repositories.
 *
 * @see updateVersion
 */
class UpdateRemote(private val libraries: List<LibraryToUpdate>,
                   private val credentials: CredentialsProvider) : PipelineOperation {

    override fun perform(libraries: Set<Library>): OperationResult {
        for (library in libraries) {
            val commands = updateVersion(remoteLibrary(library), credentials)
            commands.forEach { it.execute() }
        }
        return Ok
    }

    private fun remoteLibrary(localLibrary: Library): LibraryToUpdate {
        return libraries.find { it.local == localLibrary }!!
    }
}
