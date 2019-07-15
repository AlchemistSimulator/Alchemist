package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Base class for [GeometricTransformation] providing a standard implementation for
 * [GeometricShapeFactory.adimensional].
 */
abstract class AbstractShapeFactory<S : Vector<S>, A : GeometricTransformation<S>> : GeometricShapeFactory<S, A> {
    /**
     * The default origin for the shapes.
     */
    protected abstract val origin: S

    override fun adimensional() = AdimensionalShape<S, A>(origin)
}
