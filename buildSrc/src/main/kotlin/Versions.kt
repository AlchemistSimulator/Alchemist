import java.lang.IllegalArgumentException
import java.util.IllegalFormatException
import kotlin.String

@ExperimentalUnsignedTypes
data class Version(val major: ULong, val minor: ULong, val patch: ULong, val preRelease: String) : Comparable<Version> {
    override fun compareTo(other: Version) = when {
        major != other.major -> major.compareTo(other.major)
        minor != other.minor -> minor.compareTo(other.minor)
        patch != other.patch -> patch.compareTo(other.patch)
        else -> preRelease.compareTo(other.preRelease)
    }

    val isPreRelease get() = preRelease.isNotBlank()

    companion object {
        private val semVerMatcher = """(\d+)\.(\d+)\.(\d+)((?:[-+]\w+(?:\.\w+)*)*)""".toRegex()
        fun String.toVersion() = semVerMatcher.find(this)
            ?.run { destructured }
            ?.let { (major, minor, patch, preRelease) ->
                Version(major.toULong(), minor.toULong(), patch.toULong(), preRelease)
            }
            ?: throw IllegalArgumentException("Not a valid SemVer: $this")
    }
}
