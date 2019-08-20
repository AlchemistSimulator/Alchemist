package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.influencesphere.FieldOfView2D
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.smartcam.VisibleNode
import java.lang.Math.toRadians

/**
 * Checks nodes in the [env] and writes in [outputMolecule] a list of the nodes seen, filtered by those containing
 * [filterByMolecule].
 * [distance] and [angle] define the field of view.
 */
class See @JvmOverloads constructor(
    node: Node<Any>,
    private val env: EuclideanPhysics2DEnvironment<Any>,
    /**
     * Distance of the field of view.
     */
    val distance: Double,
    /**
     * Angle in degrees of the field of view
     */
    val angle: Double,
    private val outputMolecule: Molecule = SimpleMolecule("vision"),
    private val filterByMolecule: Molecule? = null
) : AbstractAction<Any>(node) {

    private val fieldOfView = FieldOfView2D(env, node, distance, toRadians(angle))

    init {
        node.setConcentration(outputMolecule, emptyList<Any>())
    }

    override fun cloneAction(n: Node<Any>, r: Reaction<Any>) =
        See(n, env, distance, angle, outputMolecule, filterByMolecule)

    override fun execute() {
        //val fov = FieldOfView2D(env, env.getPosition(node), distance, angleInRadians, env.getHeading(node).asAngle())
        //var seen = fov.influencedNodes().filter { it != node }
        var seen = fieldOfView.influentialNodes()
        filterByMolecule?.run {
            seen = seen.filter { it.contains(filterByMolecule) }
        }
        node.setConcentration(outputMolecule, seen.map { VisibleNode(it, env) })
    }

    override fun getContext() = Context.LOCAL
}