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

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.file.Path

/**
 * A file that contains information about the version of the library and the
 * versions of its dependencies.
 */
class GradleVersionFile(private val projectName: LibraryName, private val rootDir: Path) {

    companion object {

        internal const val NAME = "version.gradle.kts"

        private fun findFile(dir: File): File? {
            val children = dir.listFiles()
            return children?.find { it.name == NAME }
        }
    }

    /**
     * Returns the version of the specified library if this file declares it.
     *
     * Returns `null` otherwise.
     *
     * By default, tries to read the version of the [projectName][project that contains this
     * Gradle file].
     *
     * Parameter may be specified  to read the version of a dependency.
     */
    fun version(library: LibraryName = projectName): Version? {
        return file
                .readLines()
                .map { AssignVersion.parse(it) }
                .find { e -> e?.libraryName == library }
                ?.version
    }

    /**
     * Returns the libraries that the project declaring this versions file depends on.
     */
    fun declaredDependencies(): Map<LibraryName, Version> {
        return file
                .readLines()
                .mapNotNull { AssignVersion.parse(it) }
                .filter { it.libraryName != projectName }
                .associateBy({ it.libraryName }, { it.version })
    }

    /**
     * Overrides the version of the specified library to the specified one.
     *
     * If the specified library is not found in the file, no action is performed.
     */
    fun overrideVersion(library: LibraryName, newVersion: Version) {
        overrideVersions(mapOf(library to newVersion))
    }

    /**
     * Sets the versions of the specified libraries to new versions.
     *
     * If the specified library is not found in the file, no action is performed.
     *
     * E.g. for a file
     *
     * ```kotlin
     * val base = "1.5.0"
     * ```
     *
     * `file.overrideVersions(mapOf("coreJava" to Version(1, 5, 3)))` does not change the file.
     */
    fun overrideVersions(versions: Map<LibraryName, Version>) {
        var atLeastOnceOverridden = false
        val lines = file
                .readLines()
                .map {
                    val expr = AssignVersion.parse(it)
                    if (expr != null && versions.containsKey(expr.libraryName)) {
                        atLeastOnceOverridden = true
                        val version: Version = versions.getValue(expr.libraryName)
                        val expression =
                                AssignVersion(expr.libraryName, version)
                        expression.toString()
                    } else {
                        it
                    }
                }

        if (atLeastOnceOverridden) {
            PrintWriter(FileWriter(file)).use { writer ->
                lines.forEach { line -> writer.println(line) }
                writer.println()
            }
        }
    }

    internal val file: File by lazy {
        checkNotNull(findFile(rootDir.toFile()))
    }
}
