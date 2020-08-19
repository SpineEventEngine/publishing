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

import com.google.api.client.util.Preconditions.checkArgument

/**
 * Libraries that participate in the [PublishingPipeline].
 *
 * Publishing pipeline starts with an update of a single library: [updatedLibrary]. The [rest] of
 * the libraries need to have their versions updated to the version of the [updatedLibrary].
 *
 * @param updatedLibrary the library that got its version updated
 * @param rest the libraries that need their version updated to that of [updatedLibrary]
 */
class LibrariesToPublish private constructor(val updatedLibrary: Library, val rest: Set<Library>) {

    companion object {

        /**
         * Constructs [LibrariesToPublish] from the specified set of libraries.
         *
         * Finds library with the highest version - this library was updated and initiated the
         * publishing. Sets the [updatedLibrary] to the library with the highest version, and puts
         * the rest of the libraries into [rest].
         *
         * Throws an `IllegalArgumentsException` if the [set] is empty.
         *
         * @param set a set of libraries to publish; must be non-empty
         */
        fun from(set: Set<Library>): LibrariesToPublish {
            checkArgument(set.isNotEmpty(), "Cannot update an empty set of libraries.")

            val updatedLibrary = set.maxBy { it.version() }!!
            val rest = set.filter { it != updatedLibrary }.toSet()

            return LibrariesToPublish(updatedLibrary, rest)
        }
    }

    /**
     * Returns the set of all the libraries that are being updated.
     */
    fun toSet(): Set<Library> {
        val result = rest.toMutableSet()
        result.add(updatedLibrary)
        return result
    }
}
