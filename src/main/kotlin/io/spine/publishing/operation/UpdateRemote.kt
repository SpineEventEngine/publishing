package io.spine.publishing.operation

import io.spine.publishing.Library
import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.git.*
import io.spine.publishing.operation.UpdateRemote.Companion.updateVersion
import org.eclipse.jgit.transport.CredentialsProvider

/**
 * Updates the remote library repositories.
 *
 * This operation assumes that the libraries have initialised Git repositories.
 * @param credentials the credentials to authorize the remote repository update
 *
 * @see updateVersion
 */
class UpdateRemote(private val credentials: CredentialsProvider) : PipelineOperation() {

    companion object {

        /**
         * Returns the list of Git commands to execute in order to push a version bump branch
         * to the remote repository.
         *
         * To update the version, the following Git commands are returned:
         *
         * 1) the `master` branch is checked out, as the version change is a direct `master` push;
         * 2) the `version.gradle.kts` file is staged for commit. It is expected that this file has
         * already been changed to have the correct version;
         * 3) the commit is performed;
         * 4) the local `master` branch is pushed to the respective remote repository.
         *
         * Visible for testing.
         *
         * @param library the library that has its version updated
         * @param provider the provider of the credentials to use to authorize the version update
         */
        fun updateVersion(library: Library,
                          provider: CredentialsProvider): List<GitCommand> = listOf(
                Checkout(Master(library)),
                StageFiles(VersionFile(library)),
                Commit(VersionBumpMessage(library)),
                PushToRemote(PushDestination(library, provider))
        )
    }

    override fun perform(libraries: Set<Library>): OperationResult {
        for (library in libraries) {
            val commands = updateVersion(library, credentials)
            commands.forEach { it.execute() }
        }
        return Ok
    }
}
