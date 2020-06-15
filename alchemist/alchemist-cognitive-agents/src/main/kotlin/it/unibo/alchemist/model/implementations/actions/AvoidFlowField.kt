package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.CognitivePedestrian2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment

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
open class AvoidFlowField @JvmOverloads constructor(
    environment: Euclidean2DEnvironment<Number>,
    reaction: Reaction<Number>,
    override val pedestrian: Pedestrian2D<Number>,
    targetMolecule: Molecule,
    protected val viewDepth: Double = Double.POSITIVE_INFINITY
) : AbstractFlowFieldAction(environment, reaction, pedestrian, targetMolecule) {

    override fun cloneAction(n: Node<Number>, r: Reaction<Number>): AvoidFlowField =
        AvoidFlowField(environment, r, n as Pedestrian2D<Number>, targetMolecule)

    /**
     * @returns the next relative position. The pedestrian is moved only if he/she percepts the danger
     * (either because it is in sight or due to social contagion), otherwise a zero vector is returned.
     */
    override fun nextPosition(): Euclidean2DPosition = when {
        pedestrian.wantsToEvacuate() || isDangerInSight() -> super.nextPosition()
        else -> environment.origin
    }

    override fun Sequence<Euclidean2DPosition>.selectPosition(
        layer: Layer<Number, Euclidean2DPosition>,
        currentConcentration: Double
    ): Euclidean2DPosition = this
        .let {
            layer.center()?.let { center ->
                /*
                 * If the layer has a center, probably the most suitable position is the one obtained by moving away
                 * from the center along the direction which connects the current position to the center.
                 */
                it + (currentPosition + (currentPosition - center).resized(maxWalk))
            } ?: it
        }
        .discardUnsuitablePositions(environment, pedestrian)
        .map { it to layer.concentrationIn(it) }
        .filter { it.second < currentConcentration }
        .minBy { it.second }?.first
        ?: currentPosition

    /**
     * Checks whether the center of the layer (if there's one) is in sight. If the layer has no center true is
     * returned.
     */
    @Suppress("UNCHECKED_CAST") // as? operator is safe
    private fun isDangerInSight(): Boolean = getLayerOrFail().center()?.let { center ->
        center.distanceTo(currentPosition) <= viewDepth &&
            !((environment as? EnvironmentWithObstacles<*, *, Euclidean2DPosition>)
                ?.intersectsObstacle(currentPosition, center)
                ?: false)
    } ?: true

    private fun Pedestrian<*, *, *>.wantsToEvacuate(): Boolean =
        this is CognitivePedestrian2D<*> && this.danger == targetMolecule && this.wantsToEvacuate()
}
