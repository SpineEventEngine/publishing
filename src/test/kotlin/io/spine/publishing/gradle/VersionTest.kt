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

@file:Suppress("ClassName" /* test classes names favor readability over being callable by people */)

package io.spine.publishing.gradle

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.spine.publishing.gradle.`Version should when`.ComparisonResult.EQUAL
import io.spine.publishing.gradle.`Version should when`.ComparisonResult.LARGER
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
