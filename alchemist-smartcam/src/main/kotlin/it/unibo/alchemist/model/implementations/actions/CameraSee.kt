package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.FieldOfView2D
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.smartcam.VisibleNodeImpl
import java.lang.Math.toRadians

/**
 * Checks nodes in the [environment] and writes in [outputMolecule]
 * the list of [it.unibo.alchemist.model.interfaces.VisibleNode],
 * containing [filterByMolecule].
 * [distance] and [angle] define the field of view.
 */
class CameraSee @JvmOverloads constructor(
    node: Node<Any>,
    private val environment: Physics2DEnvironment<Any>,
    /**
     * Distance of the field of view.
     */
    val distance: Double,
    /**
     * Angle in degrees of the field of view.
     */
    val angle: Double,
    private val outputMolecule: Molecule = SimpleMolecule("vision"),
    private val filterByMolecule: Molecule? = null,
) : AbstractAction<Any>(node) {

    private val fieldOfView =
        FieldOfView2D(
            environment,
            node,
            distance,
            toRadians(angle),
        )

    init {
        node.setConcentration(outputMolecule, emptyList<Any>())
    }

    override fun cloneAction(node: Node<Any>, reaction: Reaction<Any>) =
        CameraSee(node, environment, distance, angle, outputMolecule, filterByMolecule)

    override fun execute() {
        var seen = fieldOfView.influentialNodes()
        filterByMolecule?.run {
            seen = seen.filter { it.contains(filterByMolecule) }
        }
        node.setConcentration(outputMolecule, seen.map { VisibleNodeImpl(it, environment.getPosition(it)) })
    }

    override fun getContext() = Context.LOCAL
}
