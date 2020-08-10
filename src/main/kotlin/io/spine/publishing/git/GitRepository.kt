package io.spine.publishing.git

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import java.nio.file.Path

/**
 * A Git repository with a remote origin.
 *
 * @param localRootPath the top-level folder with files tracked by this repo
 * @param remote location of the upstream remote repository
 */
data class GitRepository(val localRootPath: Path, val remote: GitHubRepoUrl) {

    /**
     * Returns the local `Repository`.
     *
     * If no repository is associated with the [localRootPath], an
     * [org.eclipse.jgit.errors.RepositoryNotFoundException] is thrown.
     */
    fun localGitRepository(): Repository {
        val repoPath = localRootPath.toAbsolutePath()
                .toFile()
        return RepositoryBuilder()
                .readEnvironment()
                .setMustExist(true)
                .setWorkTree(repoPath)
                .build()
    }
}

/**
 * A location of a GitHub repository.
 *
 * @param organization the name of the organization that this repository belongs to
 * @param name the name of the repository
 */
data class GitHubRepoUrl(val organization: Organization, val name: RepositoryName)

typealias Organization = String
typealias RepositoryName = String
