/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.maps.movestrategies.target

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.maps.GPSPoint
import it.unibo.alchemist.model.maps.movestrategies.AbstractStrategyWithGPS
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy

/**
 * This strategy follows a [it.unibo.alchemist.model.Route].
 * @param reaction the reaction
 *
 *
 */
class FollowTrace<T>(private val reaction: Reaction<*>) :
    AbstractStrategyWithGPS(),
    TargetSelectionStrategy<T, GeoPosition> {
    override fun getTarget(): GPSPoint? {
        val time = reaction.tau
        checkNotNull(trace.getNextPosition(time))
        return trace.getNextPosition(time)
    }

    override fun cloneIfNeeded(destination: Node<T>, reaction: Reaction<T>): FollowTrace<T> = FollowTrace(reaction)

    private companion object {
        private const val serialVersionUID = 2L
    }
}
