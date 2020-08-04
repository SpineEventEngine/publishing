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

import io.spine.publishing.gradle.GradleVersionFile
import io.spine.publishing.gradle.Version
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import java.nio.file.Path

/**
 * A collection of files from a remote repository.
 *
 * The project uses Gradle and declares its dependencies using a `version.gradle.kts` with a
 * Spine-specific formatting.
 *
 * The library may depend on other libraries. Such dependencies are declared in the version file.
 *
 * The library contains a Git repository with a configured remote.
 *
 * @param name the name of the library
 * @param dependencies the libraries that this library depends on
 * @param rootDir the directory that contains the library
 */
data class Library(val name: LibraryName,
                   val dependencies: List<Library>,
                   val rootDir: Path,
                   val remote: GitHubRepoUrl) {

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

    /**
     * Returns a Git repository that tracks this library.
     *
     * If the library does not contain a Git repo, a `RepositoryNotFoundException` is thrown.
     */
    fun localGitRepository(): Repository {
        val repoPath = this.rootDir.toAbsolutePath()
                .toFile()
        return RepositoryBuilder()
                .readEnvironment()
                .setMustExist(true)
                .setWorkTree(repoPath)
                .build()
    }

    private fun updateVersions(libraries: Map<LibraryName, Version>,
                               newVersion: Version) {
        val toUpdate = libraries.filter { it.value < newVersion }
                .mapValues { newVersion }

        versionFile.overrideVersions(toUpdate)
    }

    internal val versionFile: GradleVersionFile by lazy {
        GradleVersionFile(name, rootDir)
    }
}

typealias LibraryName = String

/**
 * A location of a GitHub repository.
 *
 * @param organization the name of the organization that this repository belongs to
 * @param name the name of the repository
 */
data class GitHubRepoUrl(val organization: Organization, val name: RepositoryName) {

    /**
     * Returns a URL to access the GitHub repository.
     */
    fun value(): String = "https://github.com/$organization/$name.git"
}

typealias Organization = String
typealias RepositoryName = String
