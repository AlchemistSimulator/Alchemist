package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

/**
 * Abstract implementation of an action influenced by the concentration of a given molecule in the environment.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param targetMolecule
 *          the {@link Molecule} you want to know the concentration in the different positions of the environment.
 */
abstract class AbstractLayerAction(
    protected val environment: Euclidean2DEnvironment<Number>,
    reaction: Reaction<Number>,
    override val pedestrian: PedestrianProperty<Number>,
    protected val targetMolecule: Molecule,
) : AbstractSteeringAction<Number, Euclidean2DPosition, Euclidean2DTransformation>(environment, reaction, pedestrian) {

    abstract override fun cloneAction(node: Node<Number>, reaction: Reaction<Number>): AbstractLayerAction

    /**
     * @returns the layer containing [targetMolecule] or fails.
     */
    protected fun getLayerOrFail(): Layer<Number, Euclidean2DPosition> = environment.getLayer(targetMolecule)
        .orElseThrow { IllegalStateException("no layer containing $targetMolecule") }

    /**
     * @returns the center of the layer or null if there's no center.
     */
    protected fun Layer<*, Euclidean2DPosition>.center(): Euclidean2DPosition? =
        (this as? BidimensionalGaussianLayer)?.let { environment.makePosition(it.centerX, it.centerY) }

    /**
     * @returns the concentration of the layer in the given [position].
     */
    protected fun <P : Position<P>> Layer<Number, P>.concentrationIn(position: P): Double =
        getValue(position).toDouble()
}
