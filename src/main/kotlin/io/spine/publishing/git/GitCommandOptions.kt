/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.publishing.git

import io.spine.publishing.gradle.Library
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder

/**
 * Options associated with a Git command that specify its behavior.
 */
interface GitCommandOptions {

    /**
     * Returns a local repository that the respective command is associated with.
     */
    fun repository(): Repository
}

/**
 * Given a library, returns a Git repository in its root working directory.
 *
 * If the repository does not contain a Git repo, a `RepositoryNotFoundException` is thrown.
 */
fun Library.repository(): Repository {
    val repoPath = this.rootDir.toAbsolutePath().toFile()
    return RepositoryBuilder()
            .readEnvironment()
            .setMustExist(true)
            .setWorkTree(repoPath)
            .build()
}
