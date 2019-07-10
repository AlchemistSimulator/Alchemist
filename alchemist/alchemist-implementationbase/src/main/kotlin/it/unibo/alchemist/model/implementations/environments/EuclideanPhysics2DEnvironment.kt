package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape2DFactory

/**
 * An Environment supporting {@link GeometricShape2D} and collisions detection.
 * It does not allow for two nodes to overlap unless their shape is punctiform.
 *
 * TODO: getAllNodesInRange is a public method which doesn't consider the shapes..
 */
class EuclideanPhysics2DEnvironment<T>(
    override val shapeFactory: GeometricShape2DFactory<Euclidean2DPosition> = GeometricShape2DFactory.getInstance()
) : AbstractPhysics2DEnvironment<T, Euclidean2DPosition>() {

    /**
     * Makes {@Euclidean2DPosition} from coordinates.
     * @param coordinates the coordinates
     */
    override fun makePosition(vararg coordinates: Number?): Euclidean2DPosition =
        if (coordinates.size != 2)
            throw IllegalArgumentException("${coordinates.size}D coordinates were passed to a 2D environment")
        else Euclidean2DPosition(coordinates[0]!!.toDouble(), coordinates[1]!!.toDouble())

}