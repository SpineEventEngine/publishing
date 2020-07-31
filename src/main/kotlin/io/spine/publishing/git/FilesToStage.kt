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
import io.spine.publishing.localGitRepository
import org.eclipse.jgit.lib.Repository
import java.nio.file.Path

/**
 * Files to stage for commit using the `git add` command.
 *
 * The `--patch` option is not supported, i.e. for each file, all of its changes are
 * staged for commit.
 */
interface FilesToStage : GitCommandOptions {

    /** Paths to the files staged for commit. */
    fun paths(): Set<Path>
}

/**
 * Stages only the `version.gradle.kts` of the library specified to ctor.
 *
 * @param library the library that has its version file staged for commit
 */
class VersionFile(val library: Library) : FilesToStage {

    override fun paths(): Set<Path> = setOf(relativeVersionPath())

    override fun repository(): Repository = library.localGitRepository()

    private fun relativeVersionPath(): Path {
        val versionFilePath = library.versionFile.file.toPath()
        return library.rootDir.relativize(versionFilePath)
    }
}
