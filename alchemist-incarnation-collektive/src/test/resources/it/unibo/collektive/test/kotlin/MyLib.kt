package my.test

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.aggregate.api.mapNeighborhood
import it.unibo.collektive.aggregate.*
import it.unibo.collektive.stdlib.collapse.*
import it.unibo.collektive.stdlib.doubles.FieldedDoubles.plus
import kotlin.Double.Companion.POSITIVE_INFINITY

fun Aggregate<Int>.gradient(distanceSensor: CollektiveDevice<*>, source: Boolean): Double =
    share(POSITIVE_INFINITY) {
        val dist = distanceSensor.run { distances() }
        val throughNeighbor = (it + dist).neighbors.values.min()
        when {
            source -> 0.0
            else -> throughNeighbor
        }
    }
