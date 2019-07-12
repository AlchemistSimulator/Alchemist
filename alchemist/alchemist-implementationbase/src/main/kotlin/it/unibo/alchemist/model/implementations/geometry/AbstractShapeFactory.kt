package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Base class for {@link GeometricTransformation} providing a standard implementation for
 * {@link GeometricShapeFactory#adimensional}.
 */
abstract class AbstractShapeFactory<S : Vector<S>, A : GeometricTransformation<S>> : GeometricShapeFactory<S, A> {
    /**
     * The default origin for the shapes.
     */
    protected abstract val origin : S

    override fun adimensional() = AdimensionalShape<S, A>(origin)
}

class C<T>(private val env: EuclideanPhysics2DEnvironment<T>) : AbstractNode<T>(env) {
    override fun getShape() =
        env.shapeFactory.circle(5.0)

    override fun createT(): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}