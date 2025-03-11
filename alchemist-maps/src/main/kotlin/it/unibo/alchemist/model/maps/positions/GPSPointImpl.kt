/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.positions

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.maps.GPSPoint
import java.io.Serial
import java.io.Serializable

/**
 * Implementation of a GPS point with time information.
 */
data class GPSPointImpl(private val delegate: LatLongPosition, private val time: Time) :
    GPSPoint,
    GeoPosition by delegate,
    Serializable {

    constructor(latitude: Double, longitude: Double, time: Time) : this(LatLongPosition(latitude, longitude), time)

    override fun addTime(shift: Time): GPSPointImpl = GPSPointImpl(delegate, time.plus(shift))

    override fun compareTo(other: GPSPoint): Int = time.compareTo(other.time)

    override fun getTime(): Time = time

    override fun subtractTime(t: Time): GPSPointImpl = GPSPointImpl(delegate, time.minus(t))

    override fun toString(): String = "[${delegate.latitude},${delegate.longitude}]@$time"

    private companion object {
        @Serial
        private const val serialVersionUID = 2L
    }
}
