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
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.spine.publishing.gradle.VersionTest.ComparisonResult.EQUAL
import io.spine.publishing.gradle.VersionTest.ComparisonResult.LARGER
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("`Version` should when")
class VersionTest {

    @Nested
    @DisplayName("parsing")
    inner class Parsing {

        @Test
        @DisplayName("parse a valid version")
        fun parseValid() {
            val result = Version.parseFrom("1.2.3")
            assertThat(result).isNotNull()
            assertThat(result).isEqualTo(Version(1, 2, 3))
        }

        /**
         * Spine versions don't obey semver entirely.
         *
         * They instead follow a simpler `MAJOR.MINOR.PATCH` format, where all
         * versions are numbers, and no additional labels such as `rc` or
         * `SNAPSHOT` are allowed.
         *
         * See more in [Version] documentation.
         */
        @ParameterizedTest
        @DisplayName("not parse a semver-compatible version")
        @CsvSource("1.0.0-alpha", "1.0.0-alpha+1.2", "1.8.2-beta.1.13")
        fun notParseSemver(version: String) {
            assertThrows<IllegalStateException> { Version.parseFrom(version) }
        }
    }

    @Nested
    @DisplayName("comparing")
    inner class Comparing {

        @Test
        @DisplayName("use major version")
        fun useMajor() {
            compare(Version(1, 0, 0),
                    Version(0, 10, 10),
                    LARGER)
        }

        @Test
        @DisplayName("use minor version")
        fun useMinor() {
            compare(Version(1, 10, 5),
                    Version(1, 9, 15),
                    LARGER)
        }

        @Test
        @DisplayName("use patch version")
        fun usePatch() {
            compare(Version(1, 10, 15),
                    Version(1, 10, 5),
                    LARGER)
        }

        @Test
        @DisplayName("detect equal versions")
        fun detectEqual() {
            compare(Version(1, 1, 1),
                    Version(1, 1, 1),
                    EQUAL)
        }

        private fun compare(v1: Version, v2: Version, res: ComparisonResult) {
            val actualResult = v1.compareTo(v2)
            assertThat(actualResult).isEqualTo(res.number)
        }
    }

    private enum class ComparisonResult(val number: Int) {
        LARGER(1), EQUAL(0)
    }
}
