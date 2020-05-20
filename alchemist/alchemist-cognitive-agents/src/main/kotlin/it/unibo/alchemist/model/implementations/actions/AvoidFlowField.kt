package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.CognitivePedestrian2D
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.Vector2D

/**
 * Move the pedestrian towards positions of the environment with a low concentration of the target molecule.
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param targetMolecule
 *          the {@link Molecule} you want to know the concentration in the different positions of the environment.
 * @param viewDepth
 *          the depth of view of the pedestrian, defaults to infinity.
 */
open class AvoidFlowField<P> @JvmOverloads constructor(
    environment: Environment<Number, P>,
    reaction: Reaction<Number>,
    override val pedestrian: Pedestrian2D<Number>,
    targetMolecule: Molecule,
    private val viewDepth: Double = Double.POSITIVE_INFINITY
) : FlowFieldSteeringAction<P>(environment, reaction, pedestrian, targetMolecule)
    where
        P : Position2D<P>,
        P : Vector2D<P> {

    override fun cloneAction(n: Node<Number>, r: Reaction<Number>): Action<Number> =
        AvoidFlowField(env, r, n as Pedestrian2D<Number>, targetMolecule)

    /**
     * Moves the pedestrian only if he percepts the danger (either because it is in sight or
     * due to social contagion).
     */
    override fun nextPosition(): P = when {
        pedestrian.wantsToEvacuate() || isDangerInSight() -> super.nextPosition()
        else -> env.origin()
    }

    override fun Sequence<P>.selectPosition(layer: Layer<Number, P>, currentConcentration: Double): P = this
        .let {
            layer.center()?.let { center ->
                /*
                 * If the layer has a center, probably the most suitable position
                 * is the one obtained by moving away from the center along the
                 * direction which connects the current position to the center.
                 */
                it + (currentPosition + (currentPosition - center).resized(maxWalk()))
            } ?: it
        }
        .discardUnsuitablePositions(env, pedestrian)
        .map { it to layer.concentrationIn(it) }
        .filter { it.second < currentConcentration }
        .minBy { it.second }?.first ?: currentPosition

    /**
     * Checks whether the center of the layer (if there's one) is in sight. If the layer
     * has no center true is returned.
     */
    private fun isDangerInSight(): Boolean = getLayerOrFail().center()?.let {
        it.distanceTo(currentPosition) <= viewDepth &&
            (env !is EnvironmentWithObstacles<*, *, P> || !env.intersectsObstacle(currentPosition, it))
    } ?: true

    private fun Pedestrian<*>.wantsToEvacuate(): Boolean =
        this is CognitivePedestrian2D<*> && this.danger == targetMolecule && this.wantsToEvacuate()
}
