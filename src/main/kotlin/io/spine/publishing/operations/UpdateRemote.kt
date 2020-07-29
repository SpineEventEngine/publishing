package io.spine.publishing.operations

import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.git.Git
import io.spine.publishing.github.RemoteLibraryRepository
import io.spine.publishing.github.VersionUpdate
import io.spine.publishing.gradle.Library
import org.eclipse.jgit.transport.CredentialsProvider

class UpdateRemote : PipelineOperation {

    override fun perform(libraries: Set<Library>): OperationResult {
        for (library in libraries) {
            val repo = remote(library)
            val commands = VersionUpdate(repo, credentials()).pushBranch()
            Git.executeAll(commands)
        }
        return Ok(libraries)
    }

    private fun remote(library: Library): RemoteLibraryRepository = TODO()


    private fun credentials(): CredentialsProvider = TODO()
}
