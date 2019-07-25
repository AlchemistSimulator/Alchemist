package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.smartcam.FieldOfView2D
import it.unibo.alchemist.model.smartcam.VisibleTarget
import org.protelis.lang.datatype.Tuple
import org.protelis.lang.datatype.impl.ArrayTupleImpl
import java.lang.Math.toRadians

/**
 * Checks nodes in the [env] and writes in [outputMolecule] a list of the nodes seen, filtered by those containing
 * [filterByMolecule].
 * [distance] and [angle] define the field of view.
 */
class See @JvmOverloads constructor(
    node: Node<Tuple>,
    private val env: EuclideanPhysics2DEnvironment<Tuple>,
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
) : AbstractAction<Tuple>(node) {
    private val angleInRadians = toRadians(angle)
    init {
        node.setConcentration(outputMolecule, ArrayTupleImpl())
    }

    override fun cloneAction(n: Node<Tuple>, r: Reaction<Tuple>) =
        See(n, env, distance, angle, outputMolecule, filterByMolecule)

    override fun execute() {
        val fov = FieldOfView2D(env, env.getPosition(node), distance, angleInRadians, env.getHeading(node).asAngle())
        var out: Tuple = ArrayTupleImpl()
        var seen = fov.influencedNodes()
        filterByMolecule?.run {
            seen = seen.filter { it.contains(filterByMolecule) }
        }
        seen.map { VisibleTarget(node, it, env) }
            .forEach {
                out = out.append(it)
            }
        node.setConcentration(outputMolecule, out)
    }

    override fun getContext() = Context.LOCAL
}