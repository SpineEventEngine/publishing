package io.spine.publishing.gradle

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import io.spine.publishing.gradle.given.TestEnv
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Paths

@DisplayName("`DependencyBasedOrder` should")
class DependencyBasedOrderTest {

    @Test
    fun `find a dependency-safe order`() {
        val base = mockLibrary("base")
        val time = mockLibrary("time", base)
        val coreJava = mockLibrary("coreJava", base, time)

        val result = DependencyBasedOrder(setOf(coreJava, base, time)).ordered
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
    fun `find one of safe orders in an ambiguous setup`() {
        val base = mockLibrary("base")
        val time = mockLibrary("time", base)
        val coreJava = mockLibrary("coreJava", base, time)
        val clockShop = mockLibrary("clockShop", time)
        val pinkFloyd = mockLibrary("pinkFloyd", time)

        val order = DependencyBasedOrder(setOf(clockShop, pinkFloyd, base, time, coreJava)).ordered
        assertThat(order[0]).isEqualTo(base)
        assertThat(order[1]).isEqualTo(time)

        assertThat(order.subList(2, order.size)).containsOnly(clockShop, pinkFloyd, coreJava)
    }

    @Test
    fun `update the libraries to the most recent version`(@TempDir baseDir: Path,
                                                          @TempDir timeDir: Path,
                                                          @TempDir coreJavaDir: Path) {
        val movedBase = TestEnv.copyProjectDir("base", baseDir)
        val movedTime = TestEnv.copyProjectDir("time", timeDir)
        val movedCoreJava = TestEnv.copyProjectDir("core-java", coreJavaDir)

        val base = Library("base", listOf(), movedBase)
        val time = Library("time", listOf(base), movedTime)
        val coreJava = Library("coreJava", listOf(time, base), movedCoreJava)
        val graph = DependencyBasedOrder(setOf(base, time, coreJava))

        graph.updateToTheMostRecent()

        val expectedVersion = Version(1, 9, 9)
        assertEquals(base.version(), expectedVersion)
        assertEquals(coreJava.version(), expectedVersion)
        assertEquals(base.version(), expectedVersion)

        assertEquals(base.version("time"), expectedVersion)

        assertEquals(coreJava.version("time"), expectedVersion)
        assertEquals(coreJava.version("base"), expectedVersion)
    }

    private fun assertEquals(actualVersion: Version, expectedVersion: Version) {
        assertThat(actualVersion).isEqualTo(expectedVersion)
    }


    private fun mockLibrary(name: String, vararg dependencies: Library): Library {
        val path = Paths.get("") // A mock path doesn't matter as we don't access the files.
        val deps: List<Library> = dependencies.toList()
        return Library(name, deps, path)
    }
}
