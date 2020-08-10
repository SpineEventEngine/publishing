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

package io.spine.publishing.git

import io.spine.publishing.Library
import org.eclipse.jgit.lib.Repository

/**
 * Specifies the remote repository to push as well as the credentials to use while pushing.
 *
 * @param token token that authorizes the push
 */
class PushDestination(val library: Library,
                      private val token: Token) : GitCommandOptions {

    override fun repository(): Repository = library.repository.localGitRepository()

    /**
     * Returns a URL to access the GitHub repository.
     */
    fun remoteUrl(): String {
        val token = token.value
        val org = library.repository.remote.organization
        val repoName = library.repository.remote.name
        return "https://x-access-token:$token@github.com/$org/$repoName.git"
    }
}

/**
 * A string value used to authorize a remote Git operation.
 *
 * @param value the value of the token
 */
data class Token(val value: String)
