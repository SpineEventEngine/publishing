package io.spine.publishing

/**
 * A collection of libraries that depend on each other. Allows traversal in a dependency-safe
 * manner.
 */
class LibraryGraph(val libraries: Set<Library>) {

    /**
     * Returns the libraries ordered in a way that allows a dependency-safe build.
     */
    fun ordered(): List<Library> {
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

        return result
    }
}
