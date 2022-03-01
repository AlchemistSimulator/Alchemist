package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianMovementCapability
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability

/**
 * Move the agent towards a target position.
 * It is similar to [CognitiveAgentSeek] but attempts to arrive at the target position with a zero velocity.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param decelerationRadius
 *          the distance from which the pedestrian starts to decelerate.
 * @param arrivalTolerance
 *          the distance at which the pedestrian is considered arrived to the target.
 * @param target
 *          the position the pedestrian moves towards.
 */
open class CognitiveAgentArrive<T, P, A>(
    protected val env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Node<T>,
    protected val decelerationRadius: Double,
    protected val arrivalTolerance: Double,
    protected val target: P
) : AbstractSteeringActionWithTarget<T, P, A>(env, reaction, pedestrian, target)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    constructor(
        env: Environment<T, P>,
        reaction: Reaction<T>,
        pedestrian: Node<T>,
        decelerationRadius: Double,
        arrivalTolerance: Double,
        vararg coordinates: Number
    ) : this(env, reaction, pedestrian, decelerationRadius, arrivalTolerance, env.makePosition(*coordinates))

    override val maxWalk: Double get() = with((currentPosition as Vector<P>).distanceTo(target)) {
        when {
            this < arrivalTolerance -> 0.0
            this < decelerationRadius -> Speed.default * this / decelerationRadius / reaction.rate
            else -> pedestrian.asCapability<T, PedestrianMovementCapability<T>>().speed() / reaction.rate
        }
    }

    override fun cloneAction(n: Node<T>, r: Reaction<T>): CognitiveAgentArrive<T, P, A> =
        CognitiveAgentArrive(env, r, n, decelerationRadius, arrivalTolerance, target)
}
