/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.maps.routes

import com.google.common.collect.ImmutableList
import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint3D
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.maps.TimedRoute
import it.unibo.alchemist.model.maps.positions.LatLongPosition
import java.util.concurrent.TimeUnit

/**
 * Models a route on a map, built upon the information provided by a query to GraphHopper.
 */
class GraphHopperRoute(
    from: GeoPosition,
    to: GeoPosition,
    response: GHResponse,
) : TimedRoute<GeoPosition> {
    private val distance: Double
    private val time: Double
    override val points: ImmutableList<GeoPosition>

    init {
        val errors = response.errors
        if (errors.isEmpty()) {
            val bestResponse = response.best
            val points = bestResponse.points.map { it.asPosition() }
            val initDistance = from.distanceTo(points.first())
            var pointSequence: Sequence<GeoPosition> = points.asSequence()
            var actualDistance = bestResponse.distance
            if (initDistance > 0) {
                actualDistance += initDistance
                pointSequence = sequenceOf(from) + pointSequence
            }
            val endingDistance = points.last().distanceTo(to)
            if (endingDistance > 0) {
                actualDistance += endingDistance
                pointSequence += sequenceOf(to)
            }
            // m / s, times are returned in ms
            val averageSpeed = bestResponse.distance * TimeUnit.SECONDS.toMillis(1) / bestResponse.time
            time = actualDistance / averageSpeed
            distance = actualDistance
            val builder = ImmutableList.builder<GeoPosition>()
            pointSequence.forEach(builder::add)
            this.points = builder.build()
        } else {
            val firstError = errors.first()
            val exception =
                IllegalArgumentException(
                    "Failure in the GraphHopper routing system when navigating from $from to $to, " +
                        "received response:\n$response",
                    firstError,
                )
            errors.asSequence().drop(1).forEach { exception.addSuppressed(it) }
            throw exception
        }
    }

    override fun length(): Double = distance

    override fun getPoint(step: Int): GeoPosition = points[step]

    override fun getTripTime(): Double = time

    override fun iterator(): MutableIterator<GeoPosition> = points.iterator()

    override fun size(): Int = points.size

    private companion object {
        private const val serialVersionUID = 0L

        private fun GHPoint3D.asPosition() = LatLongPosition(lat, lon)
    }
}
