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

import com.google.api.client.util.Preconditions.checkArgument

/**
 * A rule that specifies how an [action] may be retried.
 *
 * @param retriesLeft how many times the action should be retried; must be non-negative
 * @param R the result of the action that is being retried
 */
abstract class RetryPolicy<R>(private var retriesLeft: Int) {

    init {
        checkArgument(retriesLeft >= 0, "Cannot create a retry policy" +
                "with a negative amount of retries. Retries specified: `${retriesLeft}`")
    }

    /**
     * Retries the [action] until it returns a satisfactory result or until the retries are
     * exhausted.
     *
     * If the action succeeds, a corresponding `R` is returned.
     *
     * Otherwise, an [onRetriesExhausted] is called.
     */
    fun retryUntilOk(): R {
        while (retriesLeft > 0) {
            retriesLeft--
            val result = action()
            if (resultOk(result)) {
                return result
            }
        }
        onRetriesExhausted()
    }

    /**
     * The action to retry.
     */
    abstract fun action(): R

    /**
     * Returns whether the result of the action is satisfactory.
     *
     * Satisfactory results are not retried.
     *
     * Unsatisfactory results are retried until the retries are exhausted.
     */
    abstract fun resultOk(result: R): Boolean

    /**
     * An action that is executed when the retries have been exhausted without ever returning a
     * satisfactory result.
     */
    abstract fun onRetriesExhausted(): Nothing
}
