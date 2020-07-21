package io.spine.publishing.gradle

/**
 * A collection of libraries that depend on each other. Allows traversal in a dependency-safe
 * manner.
 */
class LibraryGraph(private val libraries: Set<Library>) {

    /**
     * Updates all of the libraries in this graph to the specified version.
     *
     * The libraries are published to `maven-local` as they are updated.
     *
     * Once all of the libraries are updated, they are published to the remote repo as defined
     * by their `publish` task.
     *
     * @see Library.update
     * @see GradleProject.build
     * @see GradleProject.publish
     */
    fun updateAll(newVersion: Version) {
        val projects = ordered()
        forAll(projects, { it.build() }, { "Could not build project `${it.name}` locally." })
        forAll(projects, { it.publish() }, { "Could publish project `${it.name}` to remote." })
    }

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

    private fun forAll(projects: List<Library>,
                       action: (GradleProject) -> Boolean,
                       errorMessage: (Library) -> String) {
        for (library in projects) {
            val successful = action(GradleProject(library.rootDir))
            if (!successful) {
                throw IllegalStateException(errorMessage(library))
            }
        }
    }
}
