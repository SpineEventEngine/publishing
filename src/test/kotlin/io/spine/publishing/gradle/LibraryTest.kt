/*
 *
 *  * Copyright 2020, TeamDev. All rights reserved.
 *  *
 *  * Redistribution and use in source and/or binary forms, with or without
 *  * modification, must retain the above copyright notice and the following
 *  * disclaimer.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package io.spine.publishing.gradle

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.spine.publishing.gradle.given.TestEnv
import io.spine.publishing.gradle.given.TestEnv.copyProjectDir
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Paths

@DisplayName("`Library` should")
class LibraryTest {

    companion object {
        private val DEPENDENCY = LibraryName("dependency")
        private val DEPENDANT = LibraryName("dependant")

        private fun dependencyLibrary(directory: Path): Library =
                Library(DEPENDENCY, arrayListOf(), directory)


        private fun dependantLibrary(directory: Path, dependency: Library): Library {
            return Library(DEPENDANT, arrayListOf(dependency), directory)
        }
    }

    @Test
    fun `update own version`(@TempDir tempDir: Path) {
        val dependencyRootDir: Path = copyProjectDir(DEPENDENCY.value, tempDir)
        val project = dependencyLibrary(dependencyRootDir)
        val newVersion = Version(99, 99, 0)
        project.update(newVersion)

        assertThat(project.version()).isEqualTo(newVersion)
    }

    @Test
    fun `update its dependencies in its own version file`(@TempDir dependencyTempDir: Path,
                                                          @TempDir dependantTempDir: Path) {
        val dependencyRootDir: Path = copyProjectDir(DEPENDENCY.value, dependencyTempDir)
        val dependantRootDir: Path = copyProjectDir(DEPENDANT.value, dependantTempDir)

        val newVersion = Version(99, 99, 0)
        val dependencyProject = dependencyLibrary(dependencyRootDir)
        val dependantProject = dependantLibrary(dependantRootDir, dependencyProject)

        dependantProject.update(newVersion)

        assertThat(dependantProject.version()).isEqualTo(newVersion)
        assertThat(dependantProject.version(DEPENDENCY)).isEqualTo(newVersion)
    }

    @Test
    fun `not update its dependencies version files`(@TempDir dependencyTempDir: Path,
                                                    @TempDir dependantTempDir: Path) {

        val dependencyRootDir: Path = copyProjectDir(DEPENDENCY.value, dependencyTempDir)
        val dependantRootDir: Path = copyProjectDir(DEPENDANT.value, dependantTempDir)

        val newVersion = Version(99, 99, 0)
        val dependencyProject = dependencyLibrary(dependencyRootDir)
        val dependantProject = dependantLibrary(dependantRootDir, dependencyProject)

        dependantProject.update(newVersion)

        assertThat(dependantProject.version()).isEqualTo(newVersion)
        assertThat(dependantProject.version(DEPENDENCY)).isEqualTo(newVersion)

        val oldDependencyVersion = dependencyProject.version()
        assertThat(dependencyProject.version()).isEqualTo(oldDependencyVersion)
    }
}
