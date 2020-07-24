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

package io.spine.publishing.gradle

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.spine.publishing.gradle.given.TestEnv.copyDirectory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

@DisplayName("`Library` should")
class LibraryTest {

    companion object {
        private const val DEPENDENCY = "dependency"
        private const val DEPENDANT = "dependant"

        private fun dependencyLibrary(directory: Path): Library =
                Library(DEPENDENCY, arrayListOf(), directory)


        private fun dependantLibrary(directory: Path,
                                     dependency: Library): Library {
            return Library(DEPENDANT, arrayListOf(dependency), directory)
        }
    }

    @Test
    @DisplayName("update own version")
    fun updateOwn(@TempDir tempDir: Path) {
        val dependencyRootDir = copyDirectory(DEPENDENCY, tempDir)
        val project = dependencyLibrary(dependencyRootDir)
        val newVersion = Version(99, 99, 0)
        project.update(newVersion)

        assertThat(project.version()).isEqualTo(newVersion)
    }

    @Test
    @DisplayName("update its dependencies in its own version file")
    fun updateOwnVersionFile(@TempDir dependencyTempDir: Path,
                             @TempDir dependantTempDir: Path) {
        val dependencyRootDir = copyDirectory(DEPENDENCY, dependencyTempDir)
        val dependantRootDir = copyDirectory(DEPENDANT, dependantTempDir)

        val newVersion = Version(99, 99, 0)
        val dependency = dependencyLibrary(dependencyRootDir)
        val dependant = dependantLibrary(dependantRootDir, dependency)

        dependant.update(newVersion)

        assertThat(dependant.version()).isEqualTo(newVersion)
        assertThat(dependant.version(DEPENDENCY)).isEqualTo(newVersion)
    }

    @Test
    @DisplayName("not update its dependencies version files")
    fun notUpdateOtherVersionFiles(@TempDir dependencyTempDir: Path,
                                   @TempDir dependantTempDir: Path) {
        val dependencyRootDir = copyDirectory(DEPENDENCY, dependencyTempDir)
        val dependantRootDir = copyDirectory(DEPENDANT, dependantTempDir)

        val newVersion = Version(99, 99, 0)
        val dependency = dependencyLibrary(dependencyRootDir)
        val dependant = dependantLibrary(dependantRootDir, dependency)

        dependant.update(newVersion)

        assertThat(dependant.version()).isEqualTo(newVersion)
        assertThat(dependant.version(DEPENDENCY)).isEqualTo(newVersion)

        val oldDependencyVersion = dependency.version()
        assertThat(dependency.version()).isEqualTo(oldDependencyVersion)
    }
}
