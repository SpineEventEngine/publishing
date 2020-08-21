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

import com.google.common.flogger.FluentLogger
import com.google.common.io.Files
import io.spine.publishing.Application.GITHUB_APP_ID_KEY
import io.spine.publishing.github.AppId
import io.spine.publishing.github.GitHubApp
import io.spine.publishing.github.SignedJwts
import java.nio.file.Path

/**
 * The publishing application.
 *
 * See [PublishingPipeline] for the description of the publishing process.
 */
object Application {

    /**
     * The name of the property containing the GitHub ID of this application as an app installed to
     * some organization.
     *
     * The value of the property is required for running the application.
     *
     * May be set both as a JVM property (i.e. `-Dprop=value`) or as an environment variable.
     * JVM property has a higher priority. The environment variable is used if the JVM property
     * isn't set.
     */
    const val GITHUB_APP_ID_KEY = "github_app_id"

    private val privateKeyPath: Path = copyPrivateKey()

    private val appId: AppId = appId()

    @JvmStatic
    fun main(args: Array<String>) {
        info().log("Starting the publishing application. App ID: `$appId`.")
        val jwtFactory = SignedJwts(privateKeyPath)
        val gitHubApp = GitHubApp(appId, jwtFactory)
        val installationToken = gitHubApp.tokenFactory().newToken()
        PublishingPipeline(LibrariesToPublish.from(remoteLibs), installationToken).eval()
    }
}

/**
 * Returns the publishing application logger.
 */
fun logger(): FluentLogger = FluentLogger.forEnclosingClass()

/**
 * Returns a logger that logs at the `FINE` level.
 */
fun debug(): FluentLogger.Api = logger().atFine()

/**
 * Returns a logger that logs a the `SEVERE` level.
 */
fun error(): FluentLogger.Api = logger().atSevere()

/**
 * Returns a logger that logs at the `INFO` level.
 */
fun info(): FluentLogger.Api = logger().atInfo()

/**
 * Reads the application ID.
 */
fun appId(): AppId {
    return System.getProperty(GITHUB_APP_ID_KEY) ?: System.getenv(GITHUB_APP_ID_KEY)
}

private fun copyPrivateKey(): Path {
    val tempDir = Files.createTempDir()

    val privateKey = tempDir.resolve(PRIVATE_KEY_FILE_NAME)
    try {
        /** A potential NPE is handled. */
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val privateKeyBytes = Application::javaClass
                .get()
                .classLoader
                .getResource(PRIVATE_KEY_FILE_NAME)
                .readBytes()
        privateKey.writeBytes(privateKeyBytes)
        return privateKey.toPath()
    } catch (e: Exception) {
        val message = "Could not copy the decrypted key file: `$PRIVATE_KEY_FILE_NAME`."
        throw IllegalStateException(message, e)
    }
}

private const val PRIVATE_KEY_FILE_NAME = "private_key.pem"

/**
 * Local Spine libraries associated with their remote repositories.
 */
private val remoteLibs: Set<Library> = SpineLibrary.values()
        .map { it.library }
        .toSet()
