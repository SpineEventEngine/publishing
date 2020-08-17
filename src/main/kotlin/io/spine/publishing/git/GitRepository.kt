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

typealias Organization = String
typealias RepositoryName = String

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
     * @param token token to authorize the access to the remote repository
     */
    fun value(token: GitHubToken): String =
            "https://x-access-token:${token.value}@github.com/$organization/$name.git"
}

/**
 * A string value used to authorize GitHub operations.
 *
 * GitHub tokens can expire, after which they can be [refreshed][refresh].
 *
 * @param stringValue the value of the token
 * @param expiresAt the moment after which the token is no longer usable
 * @param refreshFn a function to obtain a new value of the token; used for
 * [refreshing][refresh] the token
 */
class GitHubToken(private var stringValue: String,
                  val expiresAt: Instant,
                  private val refreshFn: () -> GitHubToken) {


    /** Returns the string value of the token. */
    val value get() = stringValue

    /**
     * Returns whether the GitHub token is expired.
     *
     * Expired tokens cannot be used to authorize Git operations. If the token has expired,
     * a new one has to be generated.
     */
    val isExpired get() = now().isAfter(expiresAt)

    /**
     * Refreshes the token value.
     *
     * Mutates this `GitHubToken` instance.
     */
    fun refresh(): GitHubToken {
        return refreshFn()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GitHubToken

        val that = Token(other.stringValue, other.expiresAt)
        return Token(this.stringValue, this.expiresAt) == that
    }

    override fun hashCode(): Int = Token(stringValue, expiresAt).hashCode()

    private data class Token(private val stringValue: String, val expirationTime: Instant)
}
