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

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import io.spine.publishing.Library
import io.spine.publishing.git.GitHubRepoUrl
import io.spine.publishing.git.GitRepository
import io.spine.publishing.git.RepositoryName
import io.spine.publishing.gradle.given.TestEnv.copyDirectory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Paths

@DisplayName("`DependencyBasedOrder` should")
class OrderingTest {

    @Test
    @DisplayName("find a dependency-safe order")
    fun safeOrder() {
        val base = mockLibrary("base")
        val time = mockLibrary("time", base)
        val coreJava = mockLibrary("coreJava", base, time)

        val result = Ordering(setOf(coreJava, base, time)).byDependencies
        assertThat(result).containsExactly(base, time, coreJava)
    }

    /**
     * `time` depends on `base`, `coreJava` depends on both `time` and `base`.
     *
     * `pinkFloyd` and `clockShop` depend on `time`.
     *
     * In a setup link this, the order should be:
     *
     * 1) `base`;
     * 2) `time`;
     *
     * The order of the rest of the libraries is irrelevant.
     */
    @Test
    @DisplayName("find one of safe orders in an ambiguous setup")
    fun safeAmbiguousOrder() {
        val base = mockLibrary("base")
        val time = mockLibrary("time", base)
        val coreJava = mockLibrary("coreJava", base, time)
        val clockShop = mockLibrary("clockShop", time)
        val pinkFloyd = mockLibrary("pinkFloyd", time)

        val order = Ordering(setOf(clockShop, pinkFloyd, base, time, coreJava)).byDependencies
        assertThat(order[0]).isEqualTo(base)
        assertThat(order[1]).isEqualTo(time)

        assertThat(order.subList(2, order.size)).containsOnly(clockShop, pinkFloyd, coreJava)
    }

    @Test
    @DisplayName("find the most recent version")
    fun findMostRecent(@TempDir baseDir: Path,
                       @TempDir timeDir: Path,
                       @TempDir coreJavaDir: Path) {
        val movedBase = copyDirectory("base", baseDir)
        val movedTime = copyDirectory("time", timeDir)
        val movedCoreJava = copyDirectory("core-java", coreJavaDir)

        val base = Library("base",
                listOf(),
                GitRepository(movedBase, mockRemote("base")))
        val time = Library("time",
                listOf(base),
                GitRepository(movedTime, mockRemote("time")))
        val coreJava = Library("coreJava",
                listOf(time, base),
                GitRepository(movedCoreJava, mockRemote("core-java")))

        val mostRecentVersion = Ordering(setOf(base, time, coreJava)).mostRecentVersion()
        assertThat(mostRecentVersion).isEqualTo(Version(1, 9, 9))
    }

    private fun mockLibrary(name: String, vararg dependencies: Library): Library {
        val path = Paths.get("") // A mock path doesn't matter as files aren't accessed.
        val deps: List<Library> = dependencies.toList()
        return Library(name, deps, GitRepository(path, mockRemote(name)))
    }

    private fun mockRemote(name: RepositoryName) = GitHubRepoUrl("test-org", name)
}
