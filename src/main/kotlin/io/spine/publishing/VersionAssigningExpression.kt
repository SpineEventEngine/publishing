package io.spine.publishing

/**
 * A Kotlin expression that defines a version of a library.
 *
 * Expressions like this are defined in `version.gradle.kts` files, and must look as follows:
 *
 * ```kotlin
 * val base = "1.6.0"
 * val time = "1.6.0"
 * ```
 *
 * Note that the version must adhere to the Spine versioning policy, more in [Version] documentation.
 */
data class VersionAssigningExpression(val libraryName: LibraryName, val version: Version) {

    companion object {
        private val regex: Regex = Regex("""val (.+) = "(\d+\.\d+\.\d+)"""")

        /**
         * Tries to parse the specified expression string.
         *
         * If the expression matches the expected template, returns the name and the version of the library.
         *
         * Otherwise, returns `null`.
         */
        fun parse(rawExpression: String): VersionAssigningExpression? {
            val result = regex.find(rawExpression)
            val groups = result?.groupValues

            if (groups?.size != 3) {
                return null
            }

            val rawName = groups[1]
            val name = LibraryName(rawName)

            val rawVersion = groups[2]
            val version = rawVersion.let { Version.parseFrom(it) }

            return version?.let { VersionAssigningExpression(name, it) }
        }
    }

    override fun toString(): String {
        return """val ${libraryName.value} = "$version""""
    }
}

