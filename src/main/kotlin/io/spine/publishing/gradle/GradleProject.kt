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

import java.nio.file.Path

/**
 * A project that uses Gradle.
 *
 * Allows to execute arbitrary Gradle tasks.
 *
 * @param rootDir path to the directory that contains a Gradle project
 */
data class GradleProject(private val rootDir: Path) {

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

    /**
     * Publishes this project to the local Maven repository.
     */
    fun publishToMavenLocal(): Boolean = runCommand("publishToMavenLocal")

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
