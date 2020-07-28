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

/**
 * A number of libraries that have dependencies between them.
 *
 * Allows to [update and publish new versions][InterdependentLibraries.publish] in a
 * dependency-based order.
 */
class InterdependentLibraries(private val ordering: Ordering) {

    /**
     * Goes through the libraries one by one, updating their version, building and publishing
     * new versions them to the remote artifact repositories.
     *
     * First, updates the versions of all libraries. Then, starts building and publishing the
     * libraries in a dependency-based order. For example:
     *
     *          A --------------> B
     *           \               /
     *            --------> C <--
     * library A depends on library B and C. Library B depends on library C.
     * In such a configuration, the following happens:
     *
     * 1) C is built;
     * 2) C is published to the local Maven repo, so that it can be used for further builds;
     * 3) B is built;
     * 4) B is published to the local Maven repo;
     * 5) A is built;
     * 6) A is published to the local Maven repo;
     * 7) A, B and C are published to the remote artifact repository, as at this point it is known
     * that it's safe to build them with new versions.
     *
     * @see Ordering
     */
    fun publish(): List<Library> {
        val libraries = updateToTheMostRecent()
        for (library in libraries) {
            val project = GradleProject(library.rootDir)
            project.build()
            project.publishToMavenLocal()
        }

        libraries.map { GradleProject(it.rootDir) }
                .forEach { it.publish() }

        return libraries
    }

    /**
     * Updates the libraries to the most recent version.
     *
     * Returns the set of the libraries that were updated. This means that the libraries that
     * already has the most recent version are not included.
     *
     * @see Library.update
     */
    fun updateToTheMostRecent(): List<Library> {
        val newVersion = ordering.mostRecentVersion()
        val librariesToUpdate = ordering
                .byDependencies
                .filter { needsUpdate(it, newVersion) }
                .toList()

        librariesToUpdate.forEach { it.update(newVersion) }
        return librariesToUpdate
    }

    private fun needsUpdate(library: Library, newVersion: Version) =
            needsOwnUpdate(library, newVersion) || needsDependencyUpdate(library, newVersion)

    private fun needsOwnUpdate(library: Library, newVersion: Version) =
            library.version() < newVersion

    private fun needsDependencyUpdate(library: Library, newVersion: Version) =
            library.versionFile.declaredDependencies()
                    .any { it.value < newVersion }
}
