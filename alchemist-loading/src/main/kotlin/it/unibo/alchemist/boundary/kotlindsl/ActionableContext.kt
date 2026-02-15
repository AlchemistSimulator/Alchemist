package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition

/**
 * DSL entry-point for configuring an [Actionable] by attaching [Action]s and [Condition]s.
 *
 * This object is intended to be used with Kotlin context receivers, so that the target [Actionable] instance
 * is implicitly available in the current scope.
 *
 * Both [action] and [condition] mutate the current [Actionable] by appending the provided element to its internal
 * collections. Ordering is preserved and duplicates are allowed.
 */
object ActionableContext {

    /**
     * Appends the given [Action] to the current [Actionable].
     *
     * @param action the action to add.
     */
    context(actionable: Actionable<T>)
    fun <T> action(action: Action<T>) {
        actionable.actions += action
    }

    /**
     * Appends the given [Condition] to the current [Actionable].
     *
     * @param condition the condition to add.
     */
    context(actionable: Actionable<T>)
    fun <T> condition(condition: Condition<T>) {
        actionable.conditions += condition
    }
}
