package it.unibo.alchemist.model.interfaces.geometry

/**
 * Defines a generic transformation of a generic shape.
 * The operations allowed depend on the space the shape belongs to.
 * This interface is meant to be extended.
 */
interface GeometricTransformation<S : Vector<S>> {

    /**
     * Performs an absolute translation to the provided position.
     * @param position the new origin of the shape
     */
    fun origin(position: S)
}
