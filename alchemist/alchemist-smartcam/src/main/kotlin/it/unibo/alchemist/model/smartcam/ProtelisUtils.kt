package it.unibo.alchemist.model.smartcam

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.VisibleNode
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.protelis.AlchemistExecutionContext
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.Field
import org.protelis.lang.datatype.FunctionDefinition
import org.protelis.lang.datatype.Tuple
import org.protelis.lang.datatype.impl.ArrayTupleImpl
import org.protelis.lang.interpreter.util.JavaInteroperabilityUtils
import org.protelis.vm.ExecutionContext
import java.util.stream.Collectors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utility class for Kotlin - Protelis intercommunication.
 */
@SuppressFBWarnings("ISC_INSTANTIATE_STATIC_CLASS", justification = "Needed for protelis syntax")
class ProtelisUtils {

    @SuppressFBWarnings("ISC_INSTANTIATE_STATIC_CLASS", justification = "Needed for protelis syntax")
    companion object {

        /**
         * The algorithm to calculate the best targets for the cameras.
         */
        @JvmField
        @SuppressFBWarnings("ISC_INSTANTIATE_STATIC_CLASS", justification = "Needed for protelis syntax")
        val CameraTargetAssignmentProblem = CameraTargetAssignmentProblemForProtelis()

        /**
         * Get the position at [distance] centered in the field of fiew of the caller.
         */
        @Suppress("UNCHECKED_CAST") // unfortunately there is no way around it
        @JvmStatic
        fun getCenterOfFovAtDistance(context: AlchemistExecutionContext<Euclidean2DPosition>, distance: Double): Tuple {
            val env = context.environmentAccess
            require(env is EuclideanPhysics2DEnvironment<Any>)
            require(context.deviceUID is Node<*>)
            val node = context.deviceUID as Node<Any>
            val angle = env.getHeading(node).asAngle()
            return (env.getPosition(node) + Euclidean2DPosition(distance * cos(angle), distance * sin(angle))).toTuple()
        }

        /**
         * Returns the closest position from the caller to [target] at the given [distance].
         */
        @JvmStatic
        fun closestPositionToTargetAtDistance(context: AlchemistExecutionContext<Euclidean2DPosition>, target: Euclidean2DPosition, distance: Double) =
            closestPositionToTargetAtDistance(context.devicePosition, target, distance).toTuple()

        /**
         * Returns the elements in the [field] appearing for the least number of devices or an empty [Tuple] if the field is empty.
         * The [field] is supposed to contain a tuple of elements for each device.
         */
        @JvmStatic
        fun elementsWithLeastSources(field: Field<Tuple>): Tuple {
            val counts = mutableMapOf<Any, Int>()
            field.values().forEach { tuple ->
                tuple.forEach {
                    counts.merge(it, 1) { v1, v2 -> v1 + v2 }
                }
            }
            val min = counts.values.min() ?: Int.MAX_VALUE
            return counts.filterValues { it <= min }.keys.toTuple()
        }

        /**
         * Finds devices in [field] whose data makes [func] returns a value which can be cast to a boolean.
         * [func] is expected to have the following signature: fun func(data: Any): Any
         */
        @JvmStatic
        fun findDevicesByData(context: ExecutionContext, field: Field<*>, func: FunctionDefinition) =
            field.stream().filter {
                JavaInteroperabilityUtils.runProtelisFunctionWithJavaArguments(context, func, listOf(it.value)).toBoolean()
            }.map { it.key }.collect(Collectors.toList()).toTuple()

        /**
         * Returns the data contained in the [field] for the [device], or [default] if there is none.
         */
        @JvmStatic
        fun getDataByDevice(default: Any, field: Field<*>, device: DeviceUID) =
            if (field.containsKey(device)) field[device] ?: default else default

        /**
         * See [OverlapRelationsGraph].
         */
        @JvmStatic
        fun buildOverlapRelationsGraph(
            context: AlchemistExecutionContext<Euclidean2DPosition>,
            strengthenValue: Double,
            evaporationBaseFactor: Double,
            evaporationMovementFactor: Double) =
            OverlapRelationsGraphForProtelis(context.deviceUID, strengthenValue, evaporationBaseFactor, evaporationMovementFactor)

        /**
         * Creates a [Tuple] from any collection.
         */
        @JvmStatic
        fun toTuple(col: Collection<*>) = col.toTuple()

        /**
         * Creates a [Map] from any [Field].
         */
        @JvmStatic
        fun cloneFieldToMap(field: Field<*>) = field.toMap()!!

        /**
         * Average value of the elements in the [tuple]. The [tuple] must contain Numbers only.
         */
        @JvmStatic
        fun averageOfTuple(tuple: Tuple) =
            tuple.map<Any, Number> { require(it is Number); it }.sumByDouble { it.toDouble() } / tuple.size()

        /**
         * Check if [target#node] contains a molecule named [attribute] and its concentration can be cast to Boolean,
         * in which case it returns it, otherwise false.
         */
        @JvmStatic
        fun isTarget(target: VisibleNode<*, *>, attribute: String) =
            with(SimpleMolecule(attribute)) {
                target.node.contains(this) && target.node.getConcentration(this).toBooleanOrNull() ?: false
            }

    }
}

/**
 * See [CameraTargetAssignmentProblem].
 */
class CameraTargetAssignmentProblemForProtelis {
    companion object {
        /**
         * Just an adapter for protelis which works for Euclidean2DPosition only.
         * See [CameraTargetAssignmentProblem.solve]
         */
        @JvmStatic
        fun solve(cameras: Field<*>, targets: Tuple, maxCamerasPerDestination: Int): Map<String, VisibleNode<*, Euclidean2DPosition>> =
            CameraTargetAssignmentProblem<CameraAdapter, VisibleNode<*, Euclidean2DPosition>>().solve(
                cameras.toCameras(),
                targets.toTargets(),
                maxCamerasPerDestination) { camera, target ->
                camera.position.getDistanceTo(target.position)
            }.mapKeys { it.key.uid }

        /**
         * Just an adapter for protelis.
         * See [CameraTargetAssignmentProblem.solve]
         */
        @JvmStatic
        fun solve(
            context: ExecutionContext,
            cameras: Field<*>,
            targets: Tuple,
            maxCamerasPerDestination: Int,
            cost: FunctionDefinition
        ): Map<String, VisibleNode<*, *>> =
            CameraTargetAssignmentProblem<CameraAdapter, VisibleNode<*, *>>().solve(
                cameras.toCameras(),
                targets.toAnyTargets(),
                maxCamerasPerDestination) {
                    camera, target -> JavaInteroperabilityUtils.runProtelisFunctionWithJavaArguments(
                        context,
                        cost,
                        listOf(camera, target)) as Double
            }.mapKeys { it.key.uid }
    }
}

/**
 * See [OverlapRelationsGraph].
 */
class OverlapRelationsGraphForProtelis(
    private val myUid: DeviceUID,
    strengthenValue: Double,
    evaporationBaseFactor: Double,
    evaporationMovementFactor: Double
) {

    private val graph = OverlapRelationsGraph<DeviceUID>(strengthenValue, evaporationBaseFactor, evaporationMovementFactor)

    /**
     * See [OverlapRelationsGraph#evaporateAllLinks].
     */
    fun evaporate(): OverlapRelationsGraphForProtelis {
        graph.evaporateAllLinks(true)
        return this
    }

    /**
     * Calls [OverlapRelationsGraph#strengthenLink] for each object in common with each camera contained in the [field].
     * The [field] is supposed to contain a [Tuple] of [VisibleNode] for each device.
     */
    fun update(field: Field<Tuple>) = update(field.toMap())

    /**
     * Calls [OverlapRelationsGraph#strengthenLink] for each object in common with each camera contained in the [map].
     * The [map] is supposed to contain a [Tuple] of [VisibleNode] for each device.
     */
    fun update(map: Map<DeviceUID, Tuple>): OverlapRelationsGraphForProtelis {
        val myobjects = map[myUid]?.asIterable()?.map { require(it is VisibleNode<*, *>); it } ?: emptyList()
        map.keys.filterNot { it == myUid }.forEach { camera ->
            map[camera]?.forEach { if (myobjects.contains(it)) graph.strengthenLink(camera) }
        }
        return this
    }

    /**
     * Returns a [Tuple] of [atMost] devices with the strongest links.
     */
    fun strongestLinks(atMost: Int) =
        graph.links.entries.sortedByDescending { it.value }.take(atMost).map { it.key }.toTuple()

    /**
     * Returns devices chosen according to the Smooth strategy described in "Online Multi-object k-coverage with Mobile Smart Cameras"
     */
    fun smooth(ctx: ExecutionContext) =
        graph.links.values.max()?.let { max ->
            graph.links.entries.filter { entry ->
                val odds = (1 + entry.value) / (1 + max)
                ctx.nextRandomDouble() <= odds
            }.map { it.key }.toTuple()
        } ?: ArrayTupleImpl()

    override fun toString() = "OverlapRelationsGraph(${graph.links})"
}

/**
 * Creates a [Tuple] from any collection.
 */
private fun Collection<*>.toTuple(): Tuple = with(iterator()) { ArrayTupleImpl(*Array(size) { next() }) }

private fun <P : Position2D<P>> Position2D<P>.toTuple(): Tuple {
    return ArrayTupleImpl(x, y)
}

private fun Field<*>.toCameras() = stream().map { CameraAdapter(it.key, it.value) }.collect(Collectors.toList())

@Suppress("UNCHECKED_CAST") // it is checked
private fun Tuple.toAnyTargets(): List<VisibleNode<*, *>> =
    toList().apply {
        forEach {
            require(it is VisibleNode<*, *>) { "$it is expected to be VisibleNode but it is ${it::class}" }
        }
    } as List<VisibleNode<*, *>>

@Suppress("UNCHECKED_CAST") // it is checked
private inline fun <reified P : Position<P>> Tuple.toTargets(): List<VisibleNode<*, P>> =
    toAnyTargets().apply {
        forEach {
            require(it.position is P) { "${it.position} is expected to be ${P::class} but it is ${it.position::class}" }
        }
    } as List<VisibleNode<*, P>>

private class CameraAdapter(
    id: Any,
    pos: Any
) {
    val uid = id.toString()
    val position: Euclidean2DPosition = if (pos is Euclidean2DPosition) {
        pos
    } else {
        require(pos is Tuple && pos.size() >= 2)
        pos.toPosition()
    }

    override fun toString() =
        "Camera#$uid"
}

private fun Tuple.toPosition(): Euclidean2DPosition {
    require(size() == 2)
    val x = get(0)
    val y = get(1)
    require(x is Double && y is Double)
    return Euclidean2DPosition(x, y)
}

private fun Any?.toBooleanOrNull(): Boolean? =
    when (this) {
        null -> this
        is Boolean -> this
        is Int -> !equals(0)
        is Double -> compareTo(0.0).toBoolean()
        is Number -> toDouble().toBoolean()
        is String -> with(decapitalize()) { equals("true") || equals("on") || equals("yes") || toDoubleOrNull().toBooleanOrNull() ?: false }
        else -> null
    }

private fun Any?.toBoolean() = toBooleanOrNull() ?: throw IllegalArgumentException("Failed to convert $this to Boolean")