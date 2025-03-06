/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.movestrategies

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction

/**
 * Base class for [TargetSelectionStrategy] offering automatic target change
 * on collision and utilities for initialization.
 * [getCurrentPosition] should return the current position of the object to move.
 * [P] is the position type to use.
 */
abstract class ChangeTargetOnCollision<T, P : Position<P>>(
    /**
     * Returns the current position of the object to move.
     */
    protected val getCurrentPosition: () -> P,
) : TargetSelectionStrategy<T, P> {
    private var initialized = false
    private lateinit var lastNodePosition: P
    private lateinit var targetPosition: P

    override fun getTarget(): P {
        val currentPosition = getCurrentPosition()
        if (!initialized) {
            lastNodePosition = currentPosition
            targetPosition = currentPosition
            initializePositions(currentPosition)
            initialized = true
        }
        if (shouldChangeTarget()) {
            targetPosition = chooseTarget()
        }
        lastNodePosition = currentPosition
        return targetPosition
    }

    /**
     * Utility to initialize lazyinit variables dependent on the environment.
     * It is guaranteed to be called before [shouldChangeTarget] and [chooseTarget].
     */
    protected open fun initializePositions(currentPosition: P) = Unit

    /**
     * Returns a boolean indicating whether it is time to change the target or not.
     * By default if it hasn't moved (assuming it's because of an obstacle)
     * or or it has reached the previous, then choose another one.
     */
    protected open fun shouldChangeTarget() = with(getCurrentPosition()) {
        equals(lastNodePosition) || equals(targetPosition)
    }

    /**
     * The target selection strategy.
     * Returns the new target to reach.
     */
    protected abstract fun chooseTarget(): P

    abstract override fun cloneIfNeeded(destination: Node<T>?, reaction: Reaction<T>?): ChangeTargetOnCollision<T, P>
}
