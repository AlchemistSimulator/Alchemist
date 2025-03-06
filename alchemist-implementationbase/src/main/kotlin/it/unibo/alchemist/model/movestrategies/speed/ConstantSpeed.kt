/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.movestrategies.speed

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy
import java.io.Serial

/**
 * This strategy makes the node move at an average constant speed, which is
 * influenced by the [it.unibo.alchemist.model.TimeDistribution] of the [Reaction] hosting
 * this [it.unibo.alchemist.model.Action]. This action tries to normalize on the [Reaction]
 * rate, but if the [it.unibo.alchemist.model.TimeDistribution] has a high variance, the movements
 * on the map will inherit this tract.
 *
 * @param <T> Concentration type
 * @param <P> Position type
 */
class ConstantSpeed<T, P : Position<P>>(
    private val reaction: Reaction<*>,
    private val speed: Double,
) : SpeedSelectionStrategy<T, P> {
    /**
     * @param reaction the reaction
     * @param speed the speed, in meters/second
     */
    init {
        require(speed >= 0) { "Speed must be positive or zero in $reaction. Provided: $speed" }
    }

    override fun getNodeMovementLength(target: P?): Double = speed / reaction.rate

    override fun cloneIfNeeded(
        destination: Node<T>,
        reaction: Reaction<T>,
    ): ConstantSpeed<T, P> = ConstantSpeed(reaction, speed)

    private companion object {
        @Serial
        private const val serialVersionUID = 1746429998480123049L
    }
}
