package io.spine.publishing

/**
 * A version of a Spine library.
 *
 * @see <a href=https://spine.io/versioning/>Spine versioning policy</a>
 */
data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {

    companion object {
        private val COMPARATOR: Comparator<Version> = Comparator.comparingInt<Version> { it.major }
                .thenComparingInt { it.minor }
                .thenComparingInt { it.patch }

        fun parseFrom(stringValue: String): Version? {
            checkNotNull(stringValue)

            val versions: List<String> = stringValue.split(".")

            return if (versions.size == 3) {
                val major = versions[0].toIntOrNull()
                val minor = versions[1].toIntOrNull()
                val patch = versions[2].toIntOrNull()

                if (major != null && minor != null && patch != null) {
                    Version(major, minor, patch)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    override fun compareTo(other: Version): Int = COMPARATOR.compare(this, other)

}
