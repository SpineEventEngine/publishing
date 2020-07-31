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
 *
 * @param major the most significant version part
 * @param minor the second most significant version part
 * @param patch the least significant version part
 */
data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {

    companion object {
        private val COMPARATOR: Comparator<Version> = Comparator
                .comparingInt<Version> { it.major }
                .thenComparingInt { it.minor }
                .thenComparingInt { it.patch }

        /**
         * Returns the version represented by this string, or `null` if it doesn't represent
         * a valid Spine version.
         *
         * @param stringValue the string to parse a version from
         */
        fun parseFrom(stringValue: String): Version {
            val versions: List<String> = stringValue.split(".")

            return if (versions.size == 3) {
                val major = versions[0].toIntOrNull()
                val minor = versions[1].toIntOrNull()
                val patch = versions[2].toIntOrNull()

                if (major != null && minor != null && patch != null) {
                    Version(major, minor, patch)
                } else {
                    throw cannotParseVersion(stringValue)
                }
            } else {
                throw cannotParseVersion(stringValue)
            }
        }

        private fun cannotParseVersion(versionAsString: String) =
                IllegalStateException("Could not parse a `Version` from the specified string " +
                        "value: `$versionAsString`")
    }

    /**
     * Compares this version to the `other` one.
     *
     * The greater version has a greater major, minor or patch value.
     *
     * @param other the version to compare this one to
     */
    override fun compareTo(other: Version): Int = COMPARATOR.compare(this, other)

    override fun toString(): String {
        return """${major}.${minor}.${patch}"""
    }
}
