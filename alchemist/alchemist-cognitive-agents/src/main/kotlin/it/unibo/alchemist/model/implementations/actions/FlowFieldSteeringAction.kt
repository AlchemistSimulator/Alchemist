package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.surrounding
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import java.lang.IllegalStateException

/**
 * Generic implementation of an action influenced by the concentration of a given molecule in the environment.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param targetMolecule
 *          the {@link Molecule} you want to know the concentration in the different positions of the environment.
 */
abstract class FlowFieldSteeringAction(
    protected val env: Physics2DEnvironment<Number>,
    reaction: Reaction<Number>,
    pedestrian: Pedestrian2D<Number>,
    protected val targetMolecule: Molecule
) : SteeringActionImpl<Number, Euclidean2DPosition>(env, reaction, pedestrian) {

    override fun nextPosition(): Euclidean2DPosition = env.getLayer(targetMolecule)
        .orElseThrow { IllegalStateException("no layer containing $targetMolecule could be found") }
        .let { layer ->
            currentPosition.surrounding(env, maxWalk())
                /*
                 * Next relative position.
                 */
                .selectPosition(layer, layer.concentrationIn(currentPosition)) - currentPosition
        }

    /**
     * Selects a position from a list, given the layer and its concentration in the current position
     * of the pedestrian.
     * This function contains the policy concerning how to select the most desirable position.
     */
    abstract fun List<Euclidean2DPosition>.selectPosition(
        layer: Layer<Number, Euclidean2DPosition>,
        currentConcentration: Double
    ): Euclidean2DPosition

    /**
     * Discard the positions the node can't fit.
     */
    protected fun MutableList<Euclidean2DPosition>.discardUnsuitablePositions(): List<Euclidean2DPosition> =
        map {
            if (env is EuclideanPhysics2DEnvironmentWithObstacles<*, Number>) {
                /*
                 * Take into account obstacles
                 */
                env.next(currentPosition, it)
            } else it
        }
        .filter { env.canNodeFitPosition(pedestrian, it) }

    protected fun <P : Position<P>> Layer<Number, P>.concentrationIn(position: P): Double =
        getValue(position).toDouble()
}
