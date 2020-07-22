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

/**
 * A version of a Spine library.
 *
 * @see <a href=https://spine.io/versioning/>Spine versioning policy</a>
 */
data class Version(val major: Int,
                   val minor: Int,
                   val patch: Int) : Comparable<Version> {

    companion object {
        private val COMPARATOR: Comparator<Version> = Comparator
                .comparingInt<Version> { it.major }
                .thenComparingInt { it.minor }
                .thenComparingInt { it.patch }

        fun parseFrom(stringValue: String): Version? {
            checkNotNull(stringValue)

            val versions: List<String> = stringValue.split(".")

            return if (versions.size == 3) {
                val major = versions[0].toIntOrNull()
                val minor = versions[1].toIntOrNull()
                val patch = versions[2].toIntOrNull()

                if (major != null && minor != null && patch != null) {
                    Version(major, minor, patch)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    override fun compareTo(other: Version): Int = COMPARATOR.compare(this,
                                                                     other)

    override fun toString(): String {
        return """${major}.${minor}.${patch}"""
    }
}
