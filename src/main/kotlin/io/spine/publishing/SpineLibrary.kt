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

import io.spine.publishing.gradle.Library
import io.spine.publishing.gradle.LibraryName
import java.nio.file.Paths

/**
 * A Spine library to publish.
 *
 * @param local the local library with a local Git repository
 * @param remote the remote repository that contains this library
 */
enum class SpineLibrary(val local: Library, val remote: GitHubRepoAddress) {

    BASE(base, spineGitHubRepo("base")),
    TIME(time, spineGitHubRepo("time")),
    CORE_JAVA(coreJava, spineGitHubRepo("coreJava"));
}

private val base = Library("base", listOf(), Paths.get("base"))
private val time = Library("time", listOf(base), Paths.get("time"))
private val coreJava = Library("coreJava", listOf(base, time), Paths.get("core-java"))

private fun spineGitHubRepo(name: LibraryName) = GitHubRepoAddress(ORGANIZATION, name)
private const val ORGANIZATION = "SpineEventEngine"
