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

package io.spine.publishing.operation

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.spine.publishing.Library
import io.spine.publishing.git.GitRepository
import io.spine.publishing.given.PipelineTestEnv.sampleRemote
import io.spine.publishing.gradle.Version
import io.spine.publishing.gradle.given.TestEnv
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

@DisplayName("`UpdateVersions` should")
class UpdateVersionsTest {

    @Test
    @DisplayName("update the libraries to the most recent version")
    fun updateToMostRecent(@TempDir baseDir: Path,
                           @TempDir timeDir: Path,
                           @TempDir coreJavaDir: Path) {
        val movedBase = TestEnv.copyDirectory("base", baseDir)
        val movedTime = TestEnv.copyDirectory("time", timeDir)
        val movedCoreJava = TestEnv.copyDirectory("core-java", coreJavaDir)

        val base = Library("base",
                listOf(),
                GitRepository(movedBase, sampleRemote))
        val time = Library("time",
                listOf(base),
                GitRepository(movedTime, sampleRemote))
        val coreJava = Library("coreJava",
                listOf(time, base),
                GitRepository(movedCoreJava, sampleRemote))

        UpdateVersions().perform(setOf(base, time, coreJava))

        val expectedVersion = Version(1, 9, 9)
        assertThat(base.version()).isEqualTo(expectedVersion)
        assertThat(coreJava.version()).isEqualTo(expectedVersion)
        assertThat(base.version()).isEqualTo(expectedVersion)

        assertThat(base.version("time")).isEqualTo(expectedVersion)

        assertThat(coreJava.version("time")).isEqualTo(expectedVersion)
        assertThat(coreJava.version("base")).isEqualTo(expectedVersion)
    }

    @Test
    @DisplayName("update its dependency version when own version is the most recent one")
    fun updateOwn(@TempDir libraryPath: Path, @TempDir subLibraryPath: Path) {
        val libraryDir = TestEnv.copyDirectory("own-version-matches", libraryPath)
        val subLibraryDir = TestEnv.copyDirectory("subLibrary", subLibraryPath)

        val newVersion = Version(1, 3, 0)

        val subLibrary = Library("subLibrary",
                listOf(),
                GitRepository(subLibraryDir, sampleRemote))
        val library = Library("library",
                listOf(subLibrary),
                GitRepository(libraryDir, sampleRemote))

        UpdateVersions().perform(setOf(library, subLibrary))

        val versions = listOf(library.version(),
                library.version(subLibrary.name),
                subLibrary.version())
        versions.forEach { assertThat(it).isEqualTo(newVersion) }
    }
}
