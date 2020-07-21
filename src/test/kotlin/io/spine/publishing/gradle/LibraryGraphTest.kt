package io.spine.publishing.gradle

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Paths

@DisplayName("`LibraryGraph` should")
class LibraryGraphTest {

    @Test
    fun `find a dependency-safe order`() {
        val base = mockLibrary("base")
        val time = mockLibrary("time", base)
        val coreJava = mockLibrary("coreJava", base, time)

        val result = LibraryGraph(setOf(coreJava, base, time)).ordered()
        assertThat(result).containsExactly(base, time, coreJava)
    }

    /**
     *           +---------------+
     *           |               |
     * coreJava -+----- time ----+--- base
     *                   |
     *      pinkFloyd ---+--- clockShop
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

        val order = LibraryGraph(setOf(clockShop, pinkFloyd, base, time, coreJava)).ordered()
        assertThat(order[0]).isEqualTo(base)
        assertThat(order[1]).isEqualTo(time)

        assertThat(order.subList(2, order.size)).containsOnly(clockShop, pinkFloyd, coreJava)
    }


    private fun mockLibrary(name: String, vararg dependencies: Library): Library {
        val path = Paths.get("") // A mock path doesn't matter as we don't access the files.
        val deps: List<Library> = dependencies.toList()
        return Library(LibraryName(name), deps, path)
    }
}
