package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import kotlin.math.cos
import kotlin.math.sin

internal fun <P : Vector2D<P>> offsetPositionAtDistance(
    source: P,
    direction: P,
    distance: Double
) = with(direction.asAngle) {
    source + source.newFrom(cos(this) * distance, sin(this) * distance)
}

internal fun <P : Vector2D<P>> closestPositionToTargetAtDistance(
    source: P,
    target: P,
    distance: Double
) = offsetPositionAtDistance(target, source - target, distance)
