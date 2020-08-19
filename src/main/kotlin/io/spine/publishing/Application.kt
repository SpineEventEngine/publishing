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

import io.spine.publishing.github.AppId
import io.spine.publishing.github.GitHubApp
import io.spine.publishing.github.SignedJwts
import java.nio.file.Path
import java.nio.file.Paths

/**
 * The publishing application.
 *
 * See [PublishingPipeline] for the description of the publishing process.
 */
object Application {

    // TODO: 2020-08-12:serhii.lekariev: https://github.com/SpineEventEngine/publishing/issues/9
    private val privateKeyPath: Path = Paths.get(System.getProperty("pem_path"))
    private val appId: AppId = System.getProperty("github_app_id")

    @JvmStatic
    fun main(args: Array<String>) {
        val jwtFactory = SignedJwts(privateKeyPath)
        val gitHubApp = GitHubApp(appId, jwtFactory)
        val installationToken = gitHubApp.tokenFactory().newToken()
        PublishingPipeline(LibrariesToPublish.from(remoteLibs), installationToken).eval()
    }
}

/**
 * Local Spine libraries associated with their remote repositories.
 */
private val remoteLibs: Set<Library> = SpineLibrary.values()
        .map { it.library }
        .toSet()
