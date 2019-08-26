package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import org.protelis.lang.datatype.Tuple
import kotlin.math.cos
import kotlin.math.sin

internal inline fun <reified P : Position<P>> Any?.toPosition(env: Environment<*, P>): P = when (this) {
    is P -> this
    is Tuple -> {
        this.asIterable()
            .map<Any?, Number> {
                require(it is Number) {
                    "The Tuple must contain only Numbers but {$it::class} has been found"
                }
                it
            }.let { env.makePosition(*it.toTypedArray()) }
    }
    else -> throw IllegalArgumentException("Expected a Protelis Tuple or Euclidean2DPosition but got a ${this?.javaClass}")
}

internal fun offsetPositionAtDistance(env: Environment<*, Euclidean2DPosition>, source: Euclidean2DPosition, direction: Euclidean2DPosition, distance: Double) =
    with(direction.asAngle()) {
        source + env.makePosition(cos(this) * distance, sin(this) * distance)
    }

internal fun closestPositionToTargetAtDistance(env: Environment<*, Euclidean2DPosition>, source: Euclidean2DPosition, target: Euclidean2DPosition, distance: Double) =
    offsetPositionAtDistance(env, target, source - target, distance)