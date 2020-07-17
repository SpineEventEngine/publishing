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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@DisplayName("`GradleVersionFile` should")
class GradleVersionFileTest {

    companion object {
        val INITIAL_VALUES = mapOf(
                LibraryName("library") to Version(1, 0, 0),
                LibraryName("base") to Version(1, 5, 23),
                LibraryName("coreJava") to Version(1, 5, 23)
        )
    }

    @Test
    fun `read the library version`(@TempDir tempDir: Path) {
        val projectDir = moveResourceTo(tempDir)
        val projectName = LibraryName("library")
        val versionFile = GradleVersionFile(projectName, projectDir.toFile())

        assertAllMatch(INITIAL_VALUES, versionFile)
    }

    @Test
    fun `not do anything if the library to override does not exist in the file`(@TempDir dir: Path) {
        val projectDir = moveResourceTo(dir)
        val projectName = LibraryName("library")
        val versionFile = GradleVersionFile(projectName, projectDir.toFile())

        versionFile.overrideVersion(LibraryName("nonExistingLibrary"),
                                    Version(1, 5, 6))

        assertAllMatch(INITIAL_VALUES, versionFile)
    }

    @Test
    fun `return null when asking for a non-existent library version`(@TempDir tempDir: Path) {
        val projectDir = moveResourceTo(tempDir)
        val projectName = LibraryName("library")
        val versionFile = GradleVersionFile(projectName, projectDir.toFile())

        assertThat(versionFile.version(LibraryName("nonExistingLibrary")))
                .isNull()
    }

    @Test
    fun `retain the versions that were not overridden`(@TempDir tempDir: Path) {
        val projectDir = moveResourceTo(tempDir)
        val projectName = LibraryName("library")
        val versionFile = GradleVersionFile(projectName, projectDir.toFile())

        val base = LibraryName("base")
        val newBaseVersion = Version(2, 4, 6)
        versionFile.overrideVersion(base, newBaseVersion)

        val expectedValues = mapOf(
                base to newBaseVersion,
                LibraryName("library") to Version(1, 0, 0),
                LibraryName("coreJava") to Version(1, 5, 23)
        )

        assertAllMatch(expectedValues, versionFile)
    }

    @Test
    fun `override a library version`(@TempDir tempDir: Path) {
        val projectDir = moveResourceTo(tempDir)
        val projectName = LibraryName("library")
        val versionFile = GradleVersionFile(projectName, projectDir.toFile())

        val newVersion = Version(1, 3, 3)
        versionFile.overrideVersion(projectName, newVersion)
        assertThat(versionFile.version()).isEqualTo(newVersion)
    }

    private fun moveResourceTo(tempDir: Path): Path {
        val resourceName = "version.gradle.kts"
        val resource = javaClass.classLoader.getResource(resourceName)
        val projectDir = tempDir.resolve("project")
        val file = projectDir.resolve(resourceName)
        projectDir.toFile().mkdirs()

        Files.copy(Paths.get(resource.toURI()), file, REPLACE_EXISTING)
        return projectDir
    }

    private fun assertAllMatch(expectedValues: Map<LibraryName, Version>,
                               versionFile: GradleVersionFile) {
        expectedValues.forEach { (k, v) ->
            val parsedVersion = versionFile.version(k)
            assertThat(parsedVersion).isEqualTo(v)
        }
    }
}
