package io.spine.publishing.git

import io.spine.publishing.gradle.Library
import io.spine.publishing.localGitRepository
import org.eclipse.jgit.lib.Repository

/**
 * Describes how to perform a reset: to [which point in history to reset to][ResetTarget.ref],
 * and whether to perform a hard reset.
 */
interface ResetTarget : GitCommandOptions {

    fun ref(): String

    fun isHard(): Boolean
}

/**
 * Specifies that the repository must match the current `master` in the `origin` remote.
 *
 * `ToOriginMaster` ensures that the repository is going to contain exactly the pulled
 * master branch of the remote repository. Running a [Reset] with `ToOriginMaster` is akin
 * to running `git reset --hard origin/master`.
 *
 * @param library the library to bring back to the state of the remote `master` branch
 */
class ToOriginMaster(val library: Library) : ResetTarget {

    override fun ref(): String = "origin/master"

    override fun isHard(): Boolean = true

    override fun repository(): Repository = library.localGitRepository()
}
