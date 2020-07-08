package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.CognitivePedestrian2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.Molecule
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
class AvoidLayer @JvmOverloads constructor(
    environment: Euclidean2DEnvironment<Number>,
    reaction: Reaction<Number>,
    pedestrian: Pedestrian2D<Number>,
    targetMolecule: Molecule,
    private val viewDepth: Double = Double.POSITIVE_INFINITY
) : AbstractLayerAction(environment, reaction, pedestrian, targetMolecule) {

    private val followScalarField = getLayerOrFail().let { layer ->
        FollowScalarField(environment, reaction, pedestrian, layer.center()) {
            /*
             * Moves the pedestrian where the concentration is lower.
             */
            -layer.concentrationIn(it)
        }
    }

    override fun cloneAction(n: Pedestrian2D<Number>, r: Reaction<Number>): AvoidLayer =
        AvoidLayer(environment, r, n, targetMolecule, viewDepth)

    /**
     * @returns the next relative position. The pedestrian is moved only if he/she percepts the danger
     * (either because it is in sight or due to social contagion), otherwise a zero vector is returned.
     */
    override fun nextPosition(): Euclidean2DPosition = when {
        pedestrian.wantsToEscape() || isDangerInSight() -> followScalarField.nextPosition()
        else -> environment.origin
    }

    /**
     * Checks whether the center of the layer (if there's one) is in sight. If the layer has no center true is
     * returned.
     */
    @Suppress("UNCHECKED_CAST")
    private fun isDangerInSight(): Boolean = getLayerOrFail().center()?.let { center ->
        val currentPosition = environment.getPosition(pedestrian)
        /*
         * environment is euclidean, so if it has obstacles it must be an
         * EnvironmentWithObstacles<*, *, Euclidean2DPosition>. Since generic types can't be checked at runtime, this
         * is the best we can do.
         */
        val visualTrajectoryOccluded = (environment as? EnvironmentWithObstacles<*, *, Euclidean2DPosition>)
            ?.intersectsObstacle(currentPosition, center)
            ?: false
        center.distanceTo(currentPosition) <= viewDepth && !visualTrajectoryOccluded
    } ?: true

    private fun Pedestrian<*, *, *>.wantsToEscape(): Boolean =
        this is CognitivePedestrian2D<*> && this.danger == targetMolecule && this.wantsToEscape()
}
