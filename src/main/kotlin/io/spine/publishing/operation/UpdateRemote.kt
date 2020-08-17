package io.spine.publishing.operation

import io.spine.publishing.Library
import io.spine.publishing.Ok
import io.spine.publishing.OperationResult
import io.spine.publishing.PipelineOperation
import io.spine.publishing.git.GitHubToken
import io.spine.publishing.git.Master
import io.spine.publishing.git.StageFiles
import io.spine.publishing.git.VersionFile
import io.spine.publishing.git.GitCommand
import io.spine.publishing.git.Commit
import io.spine.publishing.git.VersionBumpMessage
import io.spine.publishing.git.PushToRemote
import io.spine.publishing.git.Checkout

/**
 * Propagates local [Library] changes to the
 * [remote upstream][io.spine.publishing.git.GitRepository.remote].
 *
 * @param token a token to authorize the remote operation
 */
class UpdateRemote(private val token: GitHubToken) : PipelineOperation() {

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
         * @param token a token to authorize the version update
         */
        internal fun updateVersion(library: Library,
                          token: GitHubToken): List<GitCommand> = listOf(
                Checkout(Master(library)),
                StageFiles(VersionFile(library)),
                Commit(VersionBumpMessage(library)),
                PushToRemote(library.repository, token)
        )
    }

    override fun perform(libraries: Set<Library>): OperationResult {
        for (library in libraries) {
            val commands = updateVersion(library, token)
            commands.forEach { it.execute() }
        }
        return Ok
    }
}
