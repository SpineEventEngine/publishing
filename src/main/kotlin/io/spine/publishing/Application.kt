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

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonJson
import com.beust.klaxon.Parser
import io.spine.publishing.git.GitHubRepoUrl
import io.spine.publishing.git.GitRepository
import io.spine.publishing.git.Token
import io.spine.publishing.operation.UpdateRemote
import io.spine.publishing.operation.UpdateVersions
import java.nio.file.Paths

/**
 * The publishing application.
 *
 * See [PublishingPipeline] for the description of the publishing process.
 */
object Application {

    @JvmStatic
    fun main(args: Array<String>) {

//        val token = Token("v1.6f359a16d709c49e2441d3ccaa09a30c1820d6ec")

//        val secondRepo = GitRepository(Paths.get("/Users/serhiilekariev/second"),
//                GitHubRepoUrl("deadby25", "second"))
//        val twoCbRepo = GitRepository(Paths.get("/Users/serhiilekariev/2cb"),
//                GitHubRepoUrl("deadby25", "2cb"))
//        val second = Library("second", listOf(), secondRepo)
//        val twoCb = Library("twoCb", listOf(second), twoCbRepo)
//
//        UpdateVersions()
//                .perform(setOf(second, twoCb))
//        UpdateRemote(token).perform(setOf(second, twoCb))
    }
}

/**
 * Local Spine libraries associated with their remote repositories.
 */
private val remoteLibs: Set<Library> = SpineLibrary.values()
        .map { it.library }
        .toSet()
