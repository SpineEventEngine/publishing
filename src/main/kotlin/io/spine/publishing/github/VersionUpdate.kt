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

import io.spine.publishing.RemoteLibrary
import io.spine.publishing.git.*
import org.eclipse.jgit.transport.CredentialsProvider

/**
 * Returns the list of Git commands to execute in order to push a version bump branch
 * to the remote repository.
 *
 * To update the version, the following Git commands are returned:
 *
 * 1) the `master` branch is checked out, as the version change is a direct `master` push;
 * 2) the `version.gradle.kts` file is staged for commit. It is expected that this file has already
 * been changed to have the correct version;
 * 3) the commit is performed;
 * 4) the local `master` branch is pushed to the respective remote repository.
 *
 * @param library the library that has its version updated
 * @param provider the provider of the credentials to use to authorize the version update
 */
fun updateVersion(library: RemoteLibrary,
                  provider: CredentialsProvider): List<GitCommand> = listOf(
        Checkout(Master(library.local)),
        StageFiles(StageVersionFile(library.local)),
        Commit(VersionBumpMessage(library.local)),
        PushToRemote(PushDestination(library, provider))
)
