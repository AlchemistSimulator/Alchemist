package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.implementations.capabilities.BasicPedestrianMovementCapability
import it.unibo.alchemist.model.implementations.groups.Alone
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianMovementCapability
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a basic pedestrian.
 *
 */
abstract class AbstractHomogeneousPedestrian<T, P, A, F> @JvmOverloads constructor(
    protected val randomGenerator: RandomGenerator,
    protected val backingNode: Node<T>,
    group: PedestrianGroup<T, P, A>? = null
) : Node<T> by backingNode, Pedestrian<T, P, A>
where
P : Position<P>, P : Vector<P>,
A : GeometricTransformation<P>,
F : GeometricShapeFactory<P, A> {
    override val membershipGroup: PedestrianGroup<T, P, A> by lazy {
        val pedestrianGroup = group?.addMember(this) as? PedestrianGroup<T, P, A>
        pedestrianGroup ?: Alone(this)
    }

    init {
        addCapability(BasicPedestrianMovementCapability())
    }

    /**
     * The speed at which the pedestrian moves if it's walking.
     */
    protected open val walkingSpeed: Double = Speed.default

    /**
     * The speed at which the pedestrian moves if it's running.
     */
    protected open val runningSpeed: Double = Speed.default * 3

    override fun speed() = randomGenerator.nextDouble(
        asCapability(PedestrianMovementCapability::class).walkingSpeed,
        asCapability(PedestrianMovementCapability::class).runningSpeed
    )
}
