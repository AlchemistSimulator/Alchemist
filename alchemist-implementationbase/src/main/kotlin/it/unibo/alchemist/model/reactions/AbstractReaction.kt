/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Dependency
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.lifecycle.Lifecycle
import it.unibo.alchemist.model.observation.lifecycle.LifecycleRegistry
import java.util.function.Supplier
import javax.annotation.Nonnull
import org.danilopianini.util.ImmutableListSet
import org.danilopianini.util.ListSet

/**
 * The type which describes the concentration of a molecule.
 * This class offers a partial implementation of Reaction. In particular, it
 * allows writing new reaction specifying only which distribution time to adopt
 *
 * @param <T> concentration type
</T> */
abstract class AbstractReaction<T>(override val node: Node<T>, override val timeDistribution: TimeDistribution<T>) :
    Reaction<T> {
    override var actions: List<Action<T>> = emptyList()

    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                canExecute = conditions.map { it.isValid() }.reduce { a, b -> a.mergeWith(b) { x, y -> x && y } }
            }
        }

    private var stringLength = Byte.MAX_VALUE.toInt()

    private val lifecycleRegistry = LifecycleRegistry()

    private var canExecute: Observable<Boolean> = MutableObservable.observe(true)

    override val rescheduleRequest: Observable<Unit> = MutableObservable.observe(Unit)

    override fun canExecute(): Observable<Boolean> = canExecute

    override fun compareTo(other: Actionable<T>): Int = tau.current.compareTo(other.tau.current)

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    /**
     * The default execution iterates all the actions in order and executes them. Override to change the behavior.
     */
    override fun execute() {
        for (a in actions) {
            a.execute()
        }
    }

    /**
     * @return a [String] representation of the rate
     */
    protected open val rateAsString: String? get() = timeDistribution.getRate().toString()

    /**
     * This method is used to provide a reaction name in toString().
     *
     * @return the name for this reaction.
     */
    protected val reactionName: String get() = javaClass.simpleName

    override val tau: Observable<Time> get() = timeDistribution.nextOccurence

    /**
     * This method is called when the environment has completed its
     * initialization. Can be used by this reaction to compute its next
     * execution time - in case such computation requires an inspection of the
     * environment. At the time this method is called, all observable
     * dependencies have been already initialized.
     * Subclasses can override this to perform custom initialization (e.g., initial update).
     *
     * @param atTime      the time at which the initialization of this reaction was
     * achieved
     * @param environment the environment
     */
    protected open fun onInitializationComplete(@Nonnull atTime: Time, @Nonnull environment: Environment<T?, *>) {
        // Empty by default
    }

    /**
     * This method provides the facility to clone reactions.
     * Given a constructor in form of a [java.util.function.Supplier], it populates the actions and conditions with
     * cloned version of the ones registered in this reaction.
     *
     * @param builder the supplier
     * @param <R>     The reaction type
     * @return the populated cloned reaction
     </R> */
    protected fun <R : Reaction<T>> makeClone(builder: Supplier<R>): R {
        val res = builder.get()
        val n = res.node
        val c = conditions.map { it.cloneCondition(n, res) }
        val a = actions.map { it.cloneAction(n, res) }
        res.actions = a
        res.conditions = c
        return res
    }

    override fun toString(): String = buildString {
        append(reactionName)
        append('@')
        append(tau)
        append(':')
        append(conditions)
        append('-')
        append(rateAsString)
        append("->")
        append(actions)
    }.also { stringLength = it.length }

    override fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: Environment<T, *>) {
//        updateInternalStatus(currentTime, hasBeenExecuted, environment)
        timeDistribution.update(currentTime, hasBeenExecuted, this, environment)
    }

//    /**
//     * This method gets called as soon as
//     * [.update] is called. It is useful to
//     * update the internal status of the reaction.
//     *
//     * @param currentTime     the current simulation time
//     * @param hasBeenExecuted true if this reaction has just been executed, false if the
//     * update has been triggered due to a dependency
//     * @param environment     the current environment
//     */
//    protected abstract fun updateInternalStatus(
//        currentTime: Time?,
//        hasBeenExecuted: Boolean,
//        environment: Environment<T, *>,
//    )

    private companion object {
        /**
         * How bigger should be the StringBuffer with respect to the previous
         * interaction.
         */
        private const val MARGIN: Byte = 20
        private val EVERYTHING: ListSet<Dependency?>? = ImmutableListSet.of<Dependency?>(Dependency.EVERYTHING)
    }
}
