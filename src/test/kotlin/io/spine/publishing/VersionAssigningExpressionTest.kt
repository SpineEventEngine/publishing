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

package io.spine.publishing

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("`VersionAssigningExpression` should")
class VersionAssigningExpressionTest {

    @Test
    fun `parse a valid expression`() {
        val validExpr = """val library = "1.0.0""""

        val expression = VersionAssigningExpression.parse(validExpr)
        assertThat(expression).isNotNull()
        assertThat(expression?.libraryName).isEqualTo(LibraryName("library"))
        assertThat(expression?.version).isEqualTo(Version(1, 0, 0))
    }

    @Test
    fun `not parse a non-constant version`() {
        val invalidExpr = """var library = "1.5.2""""

        val expression = VersionAssigningExpression.parse(invalidExpr)
        assertThat(expression).isNull()
    }

    @ParameterizedTest
    @CsvSource(
            """val library = "1"""",
            """val library = "1.0"""",
            """val library = "1.0.0-rc"""",
            """val library = "1.0.0-al"""",
            """val library = "1.2.3-alpha.1.2+build.11.e0f985a""""
    )
    fun `not parse semver-compatible versions`(rawExpr: String) {
        assertThat(VersionAssigningExpression.parse(rawExpr)).isNull()
    }

    @Test
    fun `return a parsable expression with toString()`() {
        val expression = VersionAssigningExpression(LibraryName("base"), Version(3, 0, 23))
        val toStringed = expression.toString()
        assertThat(VersionAssigningExpression.parse(toStringed)).isEqualTo(expression)
    }
}
