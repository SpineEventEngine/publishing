package io.spine.publishing.operation

import io.spine.publishing.GitHubRepoAddress
import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
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
class UpdateRemote(private val respectiveRemotes: Map<Library, GitHubRepoAddress>,
                   private val credentials: CredentialsProvider) : PipelineOperation {

    override fun perform(libraries: Set<Library>): OperationResult {
        for (library in libraries) {
            val repo = remote(library)
            val commands = updateVersion(library, repo, credentials)
            commands.forEach { it.execute() }
        }
        return Ok
    }

    @Suppress("MapGetWithNotNullAssertionOperator"/*  Not having a remote repo is a
                                                      show-stopper .*/)
    private fun remote(library: Library): GitHubRepoAddress = respectiveRemotes[library]!!
}
