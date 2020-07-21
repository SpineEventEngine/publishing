package io.spine.publishing.gradle

import java.nio.file.Path

/**
 * A project that uses Gradle.
 *
 * Allows to execute arbitrary Gradle tasks
 */
class GradleProject(private val rootDir: Path) {

    companion object {
        private const val GRADLEW = "./gradlew"
    }

    /**
     * Runs the `build` task on this project.
     *
     * Returns `false` if the task has failed.
     */
    fun build(): Boolean = runCommand("build")

    /**
     * Runs the `build` task on this project.
     *
     * Returns `false` if the task has failed.
     */
    fun publish(): Boolean = runCommand("publish")

    private fun runCommand(vararg commands: String): Boolean {
        return try {
            val actualCommands = commands.toMutableList()
            actualCommands.add(0, GRADLEW)
            val process = ProcessBuilder()
                    .command(actualCommands)
                    .directory(rootDir.toFile())
                    .inheritIO()
                    .start()

            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}
