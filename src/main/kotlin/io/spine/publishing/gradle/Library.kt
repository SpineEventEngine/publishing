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

import java.nio.file.Path

/**
 * A local project that contains the source files of a library.
 *
 * The project uses Gradle and declares its dependencies using a `version.gradle.kts` with a
 * Spine-specific formatting.
 */
data class Library(val name: LibraryName, val dependencies: List<Library>, val rootDir: Path) {

    /**
     * Updates the version of this library to the specified one.
     *
     * If this library already has the specified version, nothing is done.
     *
     * All of the dependency declarations are also updated. The version files of the libraries that
     * this library depends on are left as is.
     */
    fun update(newVersion: Version) {
        val libraries = versionFile.declaredDependencies().toMutableMap()
        libraries[this.name] = version()
        updateVersions(libraries, newVersion)
    }

    /**
     * Returns the version of the specified library as declared in the libraries version file.
     */
    fun version(libraryName: LibraryName = name): Version {
        return versionFile.version(libraryName)!!
    }

    private fun updateVersions(libraries: Map<LibraryName, Version>,
                               newVersion: Version) {
        val toUpdate = libraries
                .filter { it.value < newVersion }
                .mapValues { newVersion }

        versionFile.overrideVersions(toUpdate)
    }

    internal val versionFile: GradleVersionFile by lazy {
        GradleVersionFile(name, rootDir)
    }
}

typealias LibraryName = String
