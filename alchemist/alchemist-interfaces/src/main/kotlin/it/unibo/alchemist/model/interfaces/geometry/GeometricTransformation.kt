package it.unibo.alchemist.model.interfaces.geometry

/**
 * Defines a generic transformation of a generic shape.
 * The operations allowed are dependant on the space the shape belongs to, this interface is meant to be extended.
 */
interface GeometricTransformation<S : Vector<S>> : Function<GeometricShape<S, GeometricTransformation<S>>> {

    /**
     * Performs an absolute translation to the provided position.
     * @param position the new origin of the shape
     */
    fun origin(position: S)
}
