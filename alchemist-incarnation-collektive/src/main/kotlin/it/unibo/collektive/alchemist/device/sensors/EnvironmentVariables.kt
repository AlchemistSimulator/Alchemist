package it.unibo.collektive.alchemist.device.sensors

/**
 * Interface for generic environment variables.
 */
interface EnvironmentVariables {
    /**
     * Get the value of the variable with the given [name].
     */
    operator fun <T> get(name: String): T

    /**
     * Get the value of the variable with the given [name], or `null` if the variable is not defined.
     */
    fun <T> getOrNull(name: String): T?

    /**
     * Get the value of the variable with the given [name], or [default] if the variable is not defined.
     */
    fun <T> getOrDefault(name: String, default: T): T

    /**
     * Check if the variable with the given [name] is defined inside the environment.
     */
    fun isDefined(name: String): Boolean

    /**
     * Set the value of the variable with the given [name] to [value].
     */
    operator fun <T> set(name: String, value: T): T
}
