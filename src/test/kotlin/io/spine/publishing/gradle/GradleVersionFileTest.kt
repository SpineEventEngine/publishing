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
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.spine.publishing.LibraryName
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
                "library" to Version(1, 0, 0),
                "base" to Version(1, 5, 23),
                "coreJava" to Version(1, 5, 23)
        )
    }

    @Test
    @DisplayName("read the library version")
    fun readVersions(@TempDir tempDir: Path) {
        val versionFile = gradleVersionFile(tempDir)
        assertAllMatch(INITIAL_VALUES, versionFile)
    }

    @Test
    @DisplayName("throw an ISE upon an attempt to override a non-existing library version")
    fun doNothing(@TempDir dir: Path) {
        val versionFile = gradleVersionFile(dir)
        val newVersion = Version(1, 5, 6)
        assertThrows<IllegalStateException> {
            versionFile.updateVersions(mapOf("nonExistingLibrary" to newVersion))
        }
    }

    @Test
    @DisplayName("return `null` when asking for a non-existent library version")
    fun returnNull(@TempDir tempDir: Path) {
        val versionFile = gradleVersionFile(tempDir)
        assertThat(versionFile.version("nonExistingLibrary"))
                .isNull()
    }

    @Test
    @DisplayName("retain the versions that were not overridden")
    fun retainIfNotOverridden(@TempDir tempDir: Path) {
        val versionFile = gradleVersionFile(tempDir)

        val base = "base"
        val newBaseVersion = Version(2, 4, 6)
        versionFile.updateVersions(mapOf(base to newBaseVersion))

        val expectedValues = mapOf(
                base to newBaseVersion,
                "library" to Version(1, 0, 0),
                "coreJava" to Version(1, 5, 23)
        )

        assertAllMatch(expectedValues, versionFile)
    }

    @Test
    @DisplayName("override a library version")
    fun overrideLibraryVersion(@TempDir tempDir: Path) {
        val projectDir = moveResourceTo(tempDir)
        val projectName = "library"
        val versionFile = GradleVersionFile(projectName, projectDir)

        val newVersion = Version(1, 3, 3)
        versionFile.updateVersions(mapOf(projectName to newVersion))
        assertThat(versionFile.version()).isEqualTo(newVersion)
    }

    @Test
    @DisplayName("identify all the libraries mentioned in the versions file")
    fun identifyLibraries(@TempDir tempDir: Path) {
        val versionFile = gradleVersionFile(tempDir)

        assertThat(versionFile.declaredDependencies()
                .map { it.key })
                .containsOnly("coreJava", "base")
    }

    private fun gradleVersionFile(dir: Path): GradleVersionFile {
        val projectDir = moveResourceTo(dir)
        val projectName = "library"
        return GradleVersionFile(projectName, projectDir)
    }

    private fun moveResourceTo(tempDir: Path): Path {
        val resourceName = "version.gradle.kts"
        val resource = javaClass.classLoader.getResource(resourceName)
        val projectDir = tempDir.resolve("project")
        val file = projectDir.resolve(resourceName)
        projectDir.toFile()
                .mkdirs()

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
