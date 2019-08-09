package it.unibo.alchemist.model.smartcam

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import org.protelis.lang.datatype.Field
import org.protelis.lang.datatype.FunctionDefinition
import org.protelis.lang.datatype.Tuple
import org.protelis.lang.interpreter.util.JavaInteroperabilityUtils
import org.protelis.vm.ExecutionContext
import java.util.stream.Collectors

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
         * Average value of the elements in the [tuple]. The [tuple] must contain Numbers only.
         */
        @JvmStatic
        fun averageOfTuple(tuple: Tuple) =
            tuple.map<Any, Number> { require(it is Number); it }.sumByDouble { it.toDouble() } / tuple.size()
    }
}

/**
 * See [CameraTargetAssignmentProblem].
 */
class CameraTargetAssignmentProblemForProtelis {
    companion object {
        /**
         * Just an adapter for protelis.
         * See [CameraTargetAssignmentProblem.solve]
         */
        @JvmStatic
        fun solve(cameras: Field<*>, targets: Tuple, maxCamerasPerDestination: Int): Map<String, VisibleTarget<*>> =
            CameraTargetAssignmentProblem<CameraAdapter, VisibleTarget<*>>().solve(
                cameras.toCameras(),
                targets.toTargets(),
                maxCamerasPerDestination) {
                    camera, target -> camera.position.getDistanceTo(target.position.toPosition())
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
        ): Map<String, VisibleTarget<*>> =
            CameraTargetAssignmentProblem<CameraAdapter, VisibleTarget<*>>().solve(
                cameras.toCameras(),
                targets.toTargets(),
                maxCamerasPerDestination) {
                    camera, target -> JavaInteroperabilityUtils.runProtelisFunctionWithJavaArguments(
                        context,
                        cost,
                        listOf(camera, target)) as Double
            }.mapKeys { it.key.uid }
    }
}

private fun Field<*>.toCameras() = stream().map { CameraAdapter(it.key, it.value) }.collect(Collectors.toList())
private fun Tuple.toTargets() = toList().map { it as VisibleTarget<*> }

private class CameraAdapter(
    id: Any,
    pos: Any
) {
    val uid = id.toString()
    val position: Euclidean2DPosition
    init {
        require(pos is Tuple && pos.size() >= 2)
        position = pos.toPosition()
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