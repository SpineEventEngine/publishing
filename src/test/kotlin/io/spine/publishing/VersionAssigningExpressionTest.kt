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
}
