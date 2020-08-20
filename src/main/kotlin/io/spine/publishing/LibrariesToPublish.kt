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

import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.Preconditions.checkArgument

/**
 * Libraries that participate in the [PublishingPipeline].
 *
 * Publishing pipeline starts with an update of a number of libraries: [updatedLibraries].
 * The [rest] of the libraries need to have their versions updated to the version of the
 * [updatedLibraries].
 *
 * @param updatedLibraries the library that got their versions updated
 * @param rest the libraries that need their version updated to that of [updatedLibraries]
 */
class LibrariesToPublish private constructor(val updatedLibraries: Set<Library>,
                                             val rest: Set<Library>) {

    companion object {

        /**
         * Constructs [LibrariesToPublish] from the specified set of libraries.
         *
         * Finds the libraries that are already present in the artifact repository: those libraries
         * already have the necessary versions, and thus, they become the [updatedLibraries].
         * The rest of the libraries are added to [rest].
         *
         * Throws an `IllegalArgumentsException` if the [set] is empty.
         *
         * @param set a set of libraries to publish; must be non-empty
         * @param transport transport to use when querying the artifact repository
         */
        fun from(set: Set<Library>, transport: HttpTransport = NetHttpTransport()): LibrariesToPublish {
            checkArgument(set.isNotEmpty(), "Cannot update an empty set of libraries.")

            val updatedLibraries = set
                    .filter {
                        SpineCloudRepoArtifact(it.artifact, transport).isPublished(it.version())
                    }
                    .toSet()
            val rest = set.filter { !updatedLibraries.contains(it) }.toSet()

            return LibrariesToPublish(updatedLibraries, rest)
        }
    }

    /**
     * Returns the set of all the libraries that are being updated.
     */
    fun toSet(): Set<Library> {
        val result = rest.toMutableSet()
        result.addAll(updatedLibraries)
        return result
    }
}
