package io.spine.publishing.gradle

/**
 * A collection of libraries that depend on each other. Allows traversal in a dependency-safe
 * manner.
 */
class LibraryGraph(private val libraries: Set<Library>) {

    /**
     * Finds the most recent version among the libraries, then updates all of the libraries in
     * this graph to the most recent one.
     *
     * @see Library.update
     */
    fun updateToTheMostRecent() {
        val mostRecent = ordered.maxBy { it.version() }!!.version()
        updateAll(mostRecent)
    }

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
