/*
 *
 *  * Copyright 2020, TeamDev. All rights reserved.
 *  *
 *  * Redistribution and use in source and/or binary forms, with or without
 *  * modification, must retain the above copyright notice and the following
 *  * disclaimer.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package io.spine.publishing

import io.spine.publishing.gradle.Library
import io.spine.publishing.gradle.LibraryGraph
import io.spine.publishing.gradle.LibraryName
import java.nio.file.Paths

/**
 * The publishing application.
 *
 * Make sure that the local Spine libraries to update have the most recent master version.
 */
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        val libraries = LibraryGraph(setOf(
                SpineLibrary.BASE.library,
                SpineLibrary.TIME.library,
                SpineLibrary.CORE_JAVA.library))
        libraries.updateToTheMostRecent()

    }
}

enum class SpineLibrary(val library: Library) {
    BASE(Library(LibraryName("base"), listOf(), Paths.get("./base"))),
    TIME(Library(LibraryName("time"), listOf(BASE.library), Paths.get("./time"))),
    CORE_JAVA(Library(LibraryName("coreJava"), listOf(BASE.library, TIME.library), Paths.get("core-java")))
}