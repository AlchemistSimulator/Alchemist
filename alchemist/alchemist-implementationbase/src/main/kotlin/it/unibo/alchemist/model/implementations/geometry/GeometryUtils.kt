package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import kotlin.math.atan2

/**
 * Computes the angle with atan2(y, x)
 *
 * @return atan2(y, x) (in radians)
 */
fun Euclidean2DPosition.asAngle() = atan2(y, x)
