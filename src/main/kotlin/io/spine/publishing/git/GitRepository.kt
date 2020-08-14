package io.spine.publishing.git

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import java.nio.file.Path
import java.time.Instant
import java.time.Instant.now

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
data class GitHubRepoUrl(val organization: Organization, val name: RepositoryName) {

    /**
     * A URL that can be used to access a remote GitHub repository.
     *
     * See [here](https://docs.github.com/en/developers/apps/authenticating-with-github-apps#http-based-git-access-by-an-installation).
     *
     * @param token token to authorize the access to the remote repository
     */
    fun value(token: GitHubToken): String =
            "https://x-access-token:${token.value}@github.com/$organization/$name.git"
}

typealias Organization = String
typealias RepositoryName = String

/**
 * A string value used to authorize GitHub operations.
 *
 * GitHub tokens can expire, after which they cannot be used.
 *
 * @param value the value of the token
 * @param expiresAt the moment after which the token is no longer usable
 */
data class GitHubToken(val value: String, val expiresAt: Instant) {


    val isExpired get() = now().isAfter(expiresAt)
}
