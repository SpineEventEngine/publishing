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

package io.spine.publishing.github

import assertk.assertThat
import assertk.assertions.hasLength
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@DisplayName("`RetryPolicy` should")
class RetryPolicyTest {

    @ParameterizedTest
    @DisplayName("throw when specifying a non-positive amount of retries")
    @ValueSource(ints = [0, -4])
    fun nonPositiveRetries(retries: Int) {
        assertThrows<IllegalArgumentException> {
            object : RetryPolicy<Unit>(retries) {
                override fun action() = Unit
                override fun resultOk(result: Unit) = false
                override fun onRetriesExhausted(): Nothing = throw IllegalStateException()
            }
        }
    }

    @Test
    @DisplayName("not retry if the first action result is satisfactory")
    fun notRetrySatisfactory() {
        var sideEffectCounter = 0

        val policy = object : RetryPolicy<Unit>(30) {
            override fun action() {
                sideEffectCounter++
            }

            override fun resultOk(result: Unit): Boolean = true
            override fun onRetriesExhausted(): Nothing = throw IllegalStateException()
        }

        policy.retryUntilOk()
        // The action is performed only once: no retries.
        assertThat(sideEffectCounter).isEqualTo(1)
    }

    @Test
    @DisplayName("retry an action is the result was unsatisfactory")
    fun retryOnce() {
        val policy = object : RetryPolicy<String>(30) {

            val stringBuilder = StringBuilder()

            override fun action(): String {
                stringBuilder.append("1")
                return stringBuilder.toString()
            }

            override fun resultOk(result: String): Boolean = stringBuilder.length > 5
            override fun onRetriesExhausted(): Nothing = throw IllegalStateException()
        }

        val result = policy.retryUntilOk()
        assertThat(result).hasLength(6)
    }

    @Test
    fun throwAnException() {
        var sideEffectCounter = 0

        val retryAmount = 15

        val policy = object : RetryPolicy<Unit>(retryAmount) {
            override fun action() {
                sideEffectCounter++
            }

            override fun resultOk(result: Unit): Boolean = false

            override fun onRetriesExhausted(): Nothing = throw IllegalStateException()
        }

        assertThrows<IllegalStateException> {
            policy.retryUntilOk()
        }

        assertThat(sideEffectCounter).isEqualTo(retryAmount)
    }
}
