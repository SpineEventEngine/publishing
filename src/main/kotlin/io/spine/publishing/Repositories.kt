package io.spine.publishing

import io.spine.publishing.gradle.Library
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder


/**
 * Given a library, returns a Git repository in its root working directory.
 *
 * If the repository does not contain a Git repo, a `RepositoryNotFoundException` is thrown.
 */
fun Library.localGitRepository(): Repository {
    val repoPath = this.rootDir.toAbsolutePath()
            .toFile()
    return RepositoryBuilder()
            .readEnvironment()
            .setMustExist(true)
            .setWorkTree(repoPath)
            .build()
}

/**
 * A local library that has a matching remote repository.
 *
 * @param local the local library. Contains a Git repo
 * @param remoteAddress a remote repository that contains the library
 */
data class RemoteLibrary(val local: Library, val remoteAddress: GitHubRepoAddress)

/**
 * An address of a GitHub repository.
 *
 * @param organization the name of the organization that this repository belongs to
 * @param name the name of the repository
 */
data class GitHubRepoAddress(val organization: Organization, val name: RepositoryName) {

    /**
     * Returns a URL to access the GitHub repository.
     */
    fun asUrl(): String = "https://github.com/$organization/$name.git"
}

typealias Organization = String
typealias RepositoryName = String
