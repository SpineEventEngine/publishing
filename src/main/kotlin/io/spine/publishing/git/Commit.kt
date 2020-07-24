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

import io.spine.publishing.github.GitHubRepository
import io.spine.publishing.github.RemoteRepository
import io.spine.publishing.github.VersionBumpPullRequest
import io.spine.publishing.gradle.DependencyBasedOrder
import io.spine.publishing.gradle.Library
import java.nio.file.Path
import java.nio.file.Paths

/**
 * A single-file commit.
 *
 * Commits all of the changes in the file.
 */
interface Commit : GitCommandPayload {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val twoCbPath = Paths.get("/Users/serhiilekariev/2cb")
            val secondPath = Paths.get("/Users/serhiilekariev/second")

            val second = Library("second", listOf(), secondPath)
            val secondRepo = RemoteRepository(second, GitHubRepository("deadby25", "second"))

            val twoCb = Library("twoCb", listOf(second), twoCbPath)
            val twoCbRepo = RemoteRepository(twoCb, GitHubRepository("deadby25", "2cb"))

            DependencyBasedOrder(setOf(second, twoCb)).updateToTheMostRecent()

            val token = Token("3c13b836c0cfd698197e551137a48777bf15c74c")

            listOf(secondRepo, twoCbRepo)
                    .map { VersionBumpPullRequest(it, token.credentialsProvider()) }
                    .map { it.pushBranch() }
                    .forEach { Git.executeAll(it) }
        }
    }

    /** The message of the commit. */
    fun message(): CommitMessage

    /**
     * Path to the changed file.
     *
     * Must be relative to the [repository][GitCommandPayload.repository].
     */
    fun file(): Path
}

typealias CommitMessage = String
