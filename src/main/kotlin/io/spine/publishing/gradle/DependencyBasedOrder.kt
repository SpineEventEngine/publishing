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
 * A collection of libraries that can be traversed in the order based on dependencies between
 * the libraries.
 *
 * Such an order is the order in which the library is reached only when all of its dependencies
 * have been previously reached.
 */
class DependencyBasedOrder(private val libraries: Set<Library>) {

    /**
     * Finds the most recent version among the libraries, then updates all of the libraries in
     * this graph to the most recent one.
     *
     * @see Library.update
     */
    fun updateToTheMostRecent() {
        updateAll(mostRecentVersion())
    }

    /**
     * Returns the maximum found seen in this library graph.
     */
    fun mostRecentVersion() = ordered.maxBy { it.version() }!!.version()

    private fun updateAll(newVersion: Version) {
        val projects = ordered
        projects.forEach { it.update(newVersion) }
    }

    /**
     * Returns the libraries ordered in a way that allows a dependency-safe build.
     */
    val ordered: List<Library> by lazy {
        val visited = HashSet<Library>()
        val result = ArrayList<Library>()
        val toTraverse = libraries.toMutableList()

        fun canBuild(library: Library) = library.dependencies.isEmpty()
                || visited.containsAll(library.dependencies)

        fun loop(library: Library) {
            if (canBuild(library)) {
                toTraverse.remove(library)
                visited.add(library)
                result.add(library)
            } else {
                for (dependency in library.dependencies) {
                    loop(dependency)
                }
            }
        }

        while (toTraverse.isNotEmpty()) {
            loop(toTraverse[0])
        }

        result
    }
}
