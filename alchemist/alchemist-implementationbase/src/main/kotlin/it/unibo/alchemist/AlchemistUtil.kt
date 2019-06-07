package it.unibo.alchemist

import com.google.common.reflect.TypeToken

/**
 * Generic utilities.
 */
object AlchemistUtil {
    /**
     * Performs a cast operation, throwing a decent error message in case of failure.
     *
     * @param T destination type
     * @param what object to be cast to t
     * @return [what] as T
     */
    inline fun <reified T> cast(what: Any): T = when (what) {
        is T -> what
        else -> throw IllegalStateException("$what does not conform to the required type ${T::class.simpleName}")
    }

    /**
     * Performs a cast operation, throwing a decent error message in case of failure.
     *
     * @param destination destination type
     * @param what object to be cast to t
     * @return [what] as T
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> cast(destination: Class<T>, what: Any): T =
        if (destination.isAssignableFrom(what.javaClass)) {
            what as T
        } else {
            throw IllegalStateException("$what does not conform to the required type ${destination.simpleName}")
        }

    /**
     * Performs a cast operation, throwing a decent error message in case of failure.
     *
     * @param destination destination type
     * @param what object to be cast to t
     * @return [what] as T
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> cast(destination: TypeToken<T>, what: Any): T = cast(destination.rawType, what) as T
}
