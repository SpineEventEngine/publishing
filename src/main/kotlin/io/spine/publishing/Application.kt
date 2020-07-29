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

import io.spine.publishing.SpineLibrary.*
import io.spine.publishing.git.Token
import io.spine.publishing.github.RemoteLibraryRepository
import io.spine.publishing.gradle.Library
import io.spine.publishing.operations.*

/**
 * The publishing application.
 *
 * Make sure that the local Spine libraries are up to date before running.
 */
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        publishingPipeline(setOf(BASE.library(), TIME.library(), CORE_JAVA.library()))
                .eval()
    }
}

fun publishingPipeline(libraries: Set<Library>): LibrariesPipeline =
        LibrariesPipeline(libraries, listOf(
                UpdateToRecent(),
                UpdateVersions(),
                EnsureBuilds(),
                Publish(),
                UpdateRemote(libsToRemotes, Token("").credentialsProvider())
        ))

val libsToRemotes: Map<Library, RemoteLibraryRepository> =
        SpineLibrary.values().associate { Pair(it.repo.library, it.repo) }
