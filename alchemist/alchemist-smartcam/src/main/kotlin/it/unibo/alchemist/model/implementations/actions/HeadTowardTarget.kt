package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.smartcam.concentrationToPosition

/**
 * Reads the target coordinates from a molecule and sets the node's heading accordingly.
 * @param <T> concentration type
 * @param node the node
 * @param env the environment containing the node
 * @param target the molecule from which to read the destination in form of position or tuple
 */
class HeadTowardTarget<T>(
    node: Node<T>,
    private val env: EuclideanPhysics2DEnvironment<T>,
    private val target: Molecule
) : AbstractAction<T>(node) {

    /**
     * {@inheritDoc}
     */
    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        HeadTowardTarget(n, env, target)

    /**
     * Sets the heading of the node according to the target molecule.
     */
    override fun execute() {
        node.getConcentration(target)?.let {
            env.setHeading(node, concentrationToPosition(it) - env.getPosition(node))
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun getContext() = Context.LOCAL
}