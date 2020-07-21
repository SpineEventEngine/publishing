package io.spine.publishing.gradle.given

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Test utilities for working with resources.
 */
object TestEnv {

    fun copyProjectDir(projectName: String, tempDir: Path): Path {
        val resourceDirectory = javaClass.classLoader.getResource(projectName)
        val resourceDirPath = Paths.get(resourceDirectory!!.toURI())

        val result = tempDir.resolve(projectName)

        resourceDirPath.toFile().copyRecursively(result.toFile(), true)
        return result
    }
}
