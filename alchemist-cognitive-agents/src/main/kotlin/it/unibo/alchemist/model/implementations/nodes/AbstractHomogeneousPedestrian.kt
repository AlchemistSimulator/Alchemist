package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.capabilities.BaseSocialCapability
import it.unibo.alchemist.model.implementations.capabilities.BasePedestrianMovementCapability
import it.unibo.alchemist.model.implementations.groups.Alone
import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianMovementCapability
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.nextDouble
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a basic pedestrian.
 *
 */
abstract class AbstractHomogeneousPedestrian<T, P, A, F> @JvmOverloads constructor(
    protected val randomGenerator: RandomGenerator,
    protected val backingNode: Node<T>,
    group: Group<T>? = null
) : Node<T> by backingNode, Pedestrian<T, P, A>
where
P : Position<P>, P : Vector<P>,
A : GeometricTransformation<P>,
F : GeometricShapeFactory<P, A> {
    override val membershipGroup: Group<T> = group ?: Alone(backingNode)

    init {
        backingNode.addCapability(BasePedestrianMovementCapability(backingNode))
        backingNode.addCapability(
            BaseSocialCapability(
                backingNode,
                group?.also { it.add(backingNode) } ?: Alone(backingNode)
            )
        )
    }

    /**
     * The speed at which the pedestrian moves if it's walking.
     */
    protected open val walkingSpeed: Double =
        backingNode.asCapability<T, PedestrianMovementCapability<T>>().walkingSpeed

    /**
     * The speed at which the pedestrian moves if it's running.
     */
    protected open val runningSpeed: Double =
        backingNode.asCapability<T, PedestrianMovementCapability<T>>().runningSpeed

    override fun speed() = randomGenerator.nextDouble(walkingSpeed, runningSpeed)
}
