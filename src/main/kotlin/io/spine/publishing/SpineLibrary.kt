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

import io.spine.publishing.git.GitHubRepoUrl
import io.spine.publishing.git.GitRepository
import io.spine.publishing.git.RepositoryName
import java.nio.file.Paths

/**
 * A Spine library to publish.
 *
 * Each Spine library has an initialised Git repository and an upstream remote repository.
 */
enum class SpineLibrary(val library: Library) {

    BASE(base),
    TIME(time),
    CORE_JAVA(coreJava);
}

private val baseRepo = GitRepository(Paths.get("base"), spineGitHubRepo("base"))
private val timeRepo = GitRepository(Paths.get("time"), spineGitHubRepo("time"))
private val coreJavaRepo =
        GitRepository(Paths.get("core-java"), spineGitHubRepo("core-java"))

private val base = Library("base", listOf(), baseRepo)
private val time = Library("time", listOf(base), timeRepo)
private val coreJava = Library("coreJava", listOf(base, time), coreJavaRepo)

private fun spineGitHubRepo(name: RepositoryName) = GitHubRepoUrl(ORGANIZATION, name)
private const val ORGANIZATION = "SpineEventEngine"
