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

import io.spine.publishing.github.GitHubRepository
import io.spine.publishing.github.RemoteRepository
import io.spine.publishing.gradle.Library
import java.nio.file.Paths

/**
 * One of the Spine libraries published by this application.
 */
enum class SpineLibrary(val repo: RemoteRepository) {

    BASE(RemoteRepository(
            Library("base", listOf(), Paths.get("base")),
            GitHubRepository(ORGANIZATION, "base"))),
    TIME(RemoteRepository(
            Library("time", listOf(BASE.repo.library), Paths.get("time")),
            GitHubRepository(ORGANIZATION, "time"))),
    CORE_JAVA(RemoteRepository(
            Library("coreJava",
                    listOf(BASE.repo.library, TIME.repo.library),
                    Paths.get("core-java")),
            GitHubRepository(ORGANIZATION, "core-java")
    ))
}

private const val ORGANIZATION = "SpineEventEngine"
