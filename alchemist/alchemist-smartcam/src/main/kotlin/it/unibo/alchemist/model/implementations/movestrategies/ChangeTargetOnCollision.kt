package it.unibo.alchemist.model.implementations.movestrategies

import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Base class for [TargetSelectionStrategy] offering automatic target change on collision and utilities for initialization.
 * [getCurrentPosition] should return the current position of the object to move.
 * [P] is the position type to use.
 */
abstract class ChangeTargetOnCollision<P : Position<P>>(
    /**
     * Returns the current position of the object to move.
     */
    protected val getCurrentPosition: () -> P
) : TargetSelectionStrategy<P> {
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
    protected open fun initializePositions(currentPosition: P) { }

    /**
     * Returns a boolean indicating whether it is time to change the target or not.
     * By default if it hasn't moved (assuming it's because of an obstacle) or or it has reached the previous, then choose another one.
     */
    protected open fun shouldChangeTarget() = with(getCurrentPosition()) {
        equals(lastNodePosition) || equals(targetPosition)
    }

    /**
     * The target selection strategy.
     * Returns the new target to reach.
     */
    protected abstract fun chooseTarget(): P
}