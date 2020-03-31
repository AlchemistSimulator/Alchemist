package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import kotlin.math.cos
import kotlin.math.sin
import org.apache.commons.math3.random.RandomGenerator

internal fun RandomGenerator.randomAngle() = 2 * Math.PI * nextDouble()

internal inline fun <reified P : Position<P>> Any?.toPosition(env: Environment<*, P>): P = when (this) {
    is P -> this
    is Iterable<*> -> env.makePosition(*(
        this.map {
            when (it) {
                is Number -> it
                else -> throw IllegalStateException(
                    "The Iterable must contain only Numbers but ${it?.javaClass} has been found"
                )
            }
        }).toTypedArray())
    else -> throw IllegalArgumentException("Expected an Iterable or Euclidean2DPosition but got a ${this?.javaClass}")
}

internal fun offsetPositionAtDistance(
    env: Environment<*, Euclidean2DPosition>,
    source: Euclidean2DPosition,
    direction: Euclidean2DPosition,
    distance: Double
) = with(direction.asAngle()) {
    source + env.makePosition(cos(this) * distance, sin(this) * distance)
}

internal fun closestPositionToTargetAtDistance(
    env: Environment<*, Euclidean2DPosition>,
    source: Euclidean2DPosition,
    target: Euclidean2DPosition,
    distance: Double
) = offsetPositionAtDistance(env, target, source - target, distance)
