package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import org.protelis.lang.datatype.Tuple
import kotlin.math.cos
import kotlin.math.sin

internal fun concentrationToPosition(c: Any?): Euclidean2DPosition {
    require(c != null) { "Cannot read the position from null" }
    val x: Double
    val y: Double
    when (c) {
        is Tuple -> {
            require(c.size() == 2) { "Need 2 elements (x,y) but got ${c.size()}" }
            val tx = c[0]
            val ty = c[1]
            require(tx is Number && ty is Number) { "Need 2 numbers (x,y) but got ${tx::class.simpleName} and ${ty::class.simpleName}" }
            x = tx.toDouble()
            y = ty.toDouble()
        }
        is Euclidean2DPosition -> {
            x = c.x
            y = c.y
        }
        else -> throw IllegalArgumentException("Expected a Protelis Tuple or Euclidean2DPosition but got a ${c::class.simpleName}")
    }
    return Euclidean2DPosition(x, y)
}

internal fun offsetPositionAtDistance(source: Euclidean2DPosition, direction: Euclidean2DPosition, distance: Double) =
    with(direction.asAngle()) {
        source + Euclidean2DPosition(cos(this) * distance, sin(this) * distance)
    }

internal fun closestPositionToTargetAtDistance(source: Euclidean2DPosition, target: Euclidean2DPosition, distance: Double) =
    offsetPositionAtDistance(target, source - target, distance)