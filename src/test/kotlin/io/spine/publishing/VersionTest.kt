@file:Suppress("ClassName" /* test classes names favor readability over being callable by people */)

package io.spine.publishing

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.spine.publishing.`Version should when`.ComparisonResult.EQUAL
import io.spine.publishing.`Version should when`.ComparisonResult.LARGER
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class `Version should when` {

    @Nested
    inner class parsing {

        @Test
        fun `parse a valid version`() {
            val result = Version.parseFrom("1.2.3")
            assertThat(result).isNotNull()
            assertThat(result).isEqualTo(Version(1, 2, 3))
        }

        /**
         * Spine versions don't obey semver entirely.
         *
         * They instead follow a simpler `MAJOR.MINOR.PATCH` format, where all versions are numbers, and no additional
         * labels such as `rc` or `SNAPSHOT` are allowed.
         *
         * See more in [Version] documentation.
         */
        @ParameterizedTest
        @CsvSource("1.0.0-alpha", "1.0.0-alpha+1.2", "1.8.2-beta.1.13")
        fun `not parse a semver-compatible version`(version: String) {
            assertThat(Version.parseFrom(version)).isNull()
        }
    }

    @Nested
    inner class comparing {

        @Test
        fun `use major version`() {
            compare(Version(1, 0, 0),
                    Version(0, 10, 10),
                    LARGER)
        }

        @Test
        fun `use minor version`() {
            compare(Version(1, 10, 5),
                    Version(1, 9, 15),
                    LARGER)
        }

        @Test
        fun `use patch version`() {
            compare(Version(1, 10, 15),
                    Version(1, 10, 5),
                    LARGER)
        }

        @Test
        fun `detect equal versions`() {
            compare(Version(1, 1, 1),
                    Version(1, 1, 1),
                    EQUAL)
        }

        private fun compare(v1: Version, v2: Version, result: ComparisonResult) {
            val actualResult = v1.compareTo(v2)
            assertThat(actualResult).isEqualTo(result.number)
        }

    }

    private enum class ComparisonResult(val number: Int) {
        LARGER(1), EQUAL(0)
    }
}
