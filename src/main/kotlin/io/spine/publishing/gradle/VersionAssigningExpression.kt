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
 *
 */

package io.spine.publishing.gradle

/**
 * A Kotlin expression that defines a version of a library.
 *
 * Expressions like this are defined in `version.gradle.kts` files, and must look as follows:
 *
 * ```kotlin
 * val base = "1.6.0"
 * val time = "1.6.0"
 * ```
 *
 * Note that the version must adhere to the Spine versioning policy, more in [Version] documentation.
 */
data class VersionAssigningExpression(val libraryName: LibraryName, val version: Version) {

    companion object {

        // TODO: 2020-07-20:serhii.lekariev: https://github.com/SpineEventEngine/publishing/issues/4
        private val regex: Regex = Regex("""val (.+) = "(\d+\.\d+\.\d+)"""")

        /**
         * Tries to parse the specified expression string.
         *
         * If the expression matches the expected template, returns the name and the version of the library.
         *
         * Otherwise, returns `null`.
         */
        fun parse(rawExpression: String): VersionAssigningExpression? {
            val result = regex.find(rawExpression)
            val groups = result?.groupValues

            if (groups?.size != 3) {
                return null
            }

            val name = groups[1]

            val rawVersion = groups[2]
            val version = rawVersion.let { Version.parseFrom(it) }

            return version?.let { VersionAssigningExpression(name, it) }
        }
    }

    override fun toString(): String {
        return """val $libraryName = "$version""""
    }
}
