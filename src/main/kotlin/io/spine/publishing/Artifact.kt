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

import com.google.api.client.util.Preconditions.checkState

/**
 * A part of a Maven group ID.
 *
 *
 * For example: a group ID "org.apache.maven.plugins", consists of the following parts:
 *
 * 1) "org";
 * 2) "apache";
 * 3) "maven";
 * 4) "plugins".
 */
typealias GroupIdPart = String

/**
 * A Maven group ID.
 *
 * @param parts parts of the group ID; refer to [GroupId] docs
 */
data class GroupId(val parts: List<GroupIdPart>) {

    constructor(vararg parts: GroupIdPart) : this(parts.toList())

    init {
        checkState(parts.isNotEmpty(), "Cannot create an empty Group ID.")
    }

    override fun toString(): String = parts.joinToString(separator = ".")
}

/**
 * An artifact published to a Maven repository.
 *
 * @param groupId group ID of the artifact
 * @param artifactName the name of the artifact
 */
data class Artifact(val groupId: GroupId,
                    val artifactName: String) {

    init {
        checkState(artifactName.isNotBlank(), "Cannot create artifact" +
                " with a blank name.")
    }
}
