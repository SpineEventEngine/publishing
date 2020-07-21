package io.spine.publishing

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Paths

@DisplayName("`Library` should")
class LibraryTest {

    companion object {
        private val DEPENDENCY = LibraryName("dependency")
        private val DEPENDANT = LibraryName("dependant")

        private fun dependencyLibrary(directory: Path): Library  =
            Library(DEPENDENCY, arrayListOf(), directory)


        private fun dependantLibrary(directory: Path, dependency: Library): Library {
            return Library(DEPENDANT, arrayListOf(dependency), directory)
        }
    }

    @Test
    fun `update own version`(@TempDir tempDir: Path) {
        val dependencyRootDir: Path = copyProject(DEPENDENCY.value, tempDir)
        val project = dependencyLibrary(dependencyRootDir)
        val newVersion = Version(99, 99, 0)
        project.update(newVersion)

        assertThat(project.version()).isEqualTo(newVersion)
    }

    @Test
    fun `update its dependencies in its own version file`(@TempDir dependencyTempDir: Path,
                                                          @TempDir dependantTempDir: Path) {
        val dependencyRootDir: Path = copyProject(DEPENDENCY.value, dependencyTempDir)
        val dependantRootDir: Path = copyProject(DEPENDANT.value, dependantTempDir)

        val newVersion = Version(99, 99, 0)
        val dependencyProject = dependencyLibrary(dependencyRootDir)
        val dependantProject = dependantLibrary(dependantRootDir, dependencyProject)

        dependantProject.update(newVersion)

        assertThat(dependantProject.version()).isEqualTo(newVersion)
        assertThat(dependantProject.version(DEPENDENCY)).isEqualTo(newVersion)
    }

    @Test
    fun `not update its dependencies version files`(@TempDir dependencyTempDir: Path,
                                                    @TempDir dependantTempDir: Path) {

        val dependencyRootDir: Path = copyProject(DEPENDENCY.value, dependencyTempDir)
        val dependantRootDir: Path = copyProject(DEPENDANT.value, dependantTempDir)

        val newVersion = Version(99, 99, 0)
        val dependencyProject = dependencyLibrary(dependencyRootDir)
        val dependantProject = dependantLibrary(dependantRootDir, dependencyProject)

        dependantProject.update(newVersion)

        assertThat(dependantProject.version()).isEqualTo(newVersion)
        assertThat(dependantProject.version(DEPENDENCY)).isEqualTo(newVersion)

        val oldDependencyVersion = dependencyProject.version()
        assertThat(dependencyProject.version()).isEqualTo(oldDependencyVersion)
    }

    private fun copyProject(projectName: String, tempDir: Path): Path {
        val resourceDirectory = javaClass.classLoader.getResource(projectName)
        val resourceDirPath = Paths.get(resourceDirectory!!.toURI())

        val result = tempDir.resolve(projectName)

        resourceDirPath.toFile().copyRecursively(result.toFile(), true)
        return result
    }
}
