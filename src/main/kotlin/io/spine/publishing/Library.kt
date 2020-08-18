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

import io.spine.publishing.git.GitRepository
import io.spine.publishing.gradle.GradleVersionFile
import io.spine.publishing.gradle.Version

typealias LibraryName = String

/**
 * A collection of files from a remote repository.
 *
 * The library uses Gradle and declares its dependencies using a [`version.gradle.kts`]
 * [GradleVersionFile] with a Spine-specific formatting.
 *
 * The library may depend on other libraries. Such dependencies are declared in the version file.
 *
 * The library contains a Git repository with a configured remote.
 *
 * @param name the name of the library
 * @param dependencies the libraries that this library depends on
 * @param repository the repository that tracks this library
 */
data class Library(val name: LibraryName,
                   val dependencies: List<Library>,
                   val repository: GitRepository) {

    /**
     * Updates the version of this library to the specified one.
     *
     * If this library already has the specified version, nothing is done.
     *
     * All of the dependency declarations are also updated. The version files of the libraries that
     * this library depends on are left as is.
     *
     * @param newVersion the version that this library is updated to
     */
    fun update(newVersion: Version) {
        val libraries = versionFile.declaredDependencies()
                .toMutableMap()
        libraries[this.name] = version()
        updateVersions(libraries, newVersion)
    }

    /**
     * Returns the version of the specified library as declared in the libraries version file.
     *
     * If no name is passed, returns the library of the version itself. If a name is specified,
     * returns the version of the library as per [GradleVersionFile]. If this library does not
     * depend on the library with the specified name, an exception is thrown.
     *
     * @param libraryName the name of the version to check.
     */
    fun version(libraryName: LibraryName = name): Version {
        return versionFile.version(libraryName)!!
    }

    private fun updateVersions(libraries: Map<LibraryName, Version>,
                               newVersion: Version) {
        val toUpdate = libraries.filter { it.value < newVersion }
                .mapValues { newVersion }
        versionFile.updateVersions(toUpdate)
    }

    internal val versionFile: GradleVersionFile by lazy {
        GradleVersionFile(name, repository.localRootPath)
    }
}
