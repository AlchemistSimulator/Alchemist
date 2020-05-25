package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import kotlin.math.cos
import kotlin.math.sin

internal fun offsetPositionAtDistance(
    env: Environment<*, Euclidean2DPosition>,
    source: Euclidean2DPosition,
    direction: Euclidean2DPosition,
    distance: Double
) = with(direction.asAngle) {
    source + env.makePosition(cos(this) * distance, sin(this) * distance)
}

internal fun closestPositionToTargetAtDistance(
    env: Environment<*, Euclidean2DPosition>,
    source: Euclidean2DPosition,
    target: Euclidean2DPosition,
    distance: Double
) = offsetPositionAtDistance(env, target, source - target, distance)
