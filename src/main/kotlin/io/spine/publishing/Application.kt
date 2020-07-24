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

package io.spine.publishing

import io.spine.publishing.git.Git
import io.spine.publishing.git.Token
import io.spine.publishing.github.VersionBumpPullRequest
import io.spine.publishing.gradle.DependencyBasedOrder

/**
 * The publishing application.
 *
 * Make sure that the local Spine libraries are up to date before running.
 */
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        val spineLibs = SpineLibrary.values()

        val orderedLibraries = DependencyBasedOrder(spineLibs.map { it.repo.library }.toSet())
        val updated = orderedLibraries.updateToTheMostRecent()

        val reposForPr = spineLibs
                .filter { updated.contains(it.repo.library) }
                .map { it.repo }

        // TODO: 2020-07-24:serhii.lekariev: https://github.com/SpineEventEngine/publishing/issues/5
        val token = Token("")

        val pullRequests = reposForPr.map { VersionBumpPullRequest(it, token.credentialsProvider()) }
        for (pr in pullRequests) {
            Git.executeAll(pr.pushBranch())
            pr.mergeVersionBump()
        }
    }
}
