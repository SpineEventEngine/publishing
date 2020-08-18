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

import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_UNAUTHORIZED
import com.google.api.client.http.HttpUnsuccessfulResponseHandler
import com.google.api.client.util.Preconditions.checkArgument

/**
 * A back-off policy that specifies how the requests to the GitHub API can be retried.
 *
 * Tries to perform the request until the [retries] are exhausted or the request is successful.
 * On each retry, a JWT is [refreshed][GitHubJwt.refresh].
 *
 * Retries are performed only if the response is [STATUS_CODE_UNAUTHORIZED].
 *
 * @param retries the amount of times the request should be retried; must be a positive number
 * @param jwt a JWT that authorizes GitHub REST calls
 */
class JwtRefreshingBackOff(private var retries: Int,
                           private var jwt: GitHubJwt) :
        HttpUnsuccessfulResponseHandler {

    init {
        checkArgument(retries > 0, "`JwtRefreshingBackOff` must" +
                "perform at least once. Retries specified: `$retries`.")
    }

    override fun handleResponse(request: HttpRequest?,
                                response: HttpResponse?,
                                supportsRetry: Boolean): Boolean {
        while (retries > 0) {
            return if (request != null && response != null && unauthorized(response)) {
                retries--
                refreshJwt(request)
                true
            } else {
                false
            }
        }
        return false
    }

    private fun unauthorized(response: HttpResponse) =
            response.statusCode == STATUS_CODE_UNAUTHORIZED

    private fun refreshJwt(request: HttpRequest) {
        jwt = jwt.refresh()
        jwt.authorize(request)
    }
}
