/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.adapters.reaction

import arrow.core.getOrElse
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Dependency.EVERYTHING
import it.unibo.alchemist.model.Dependency.EVERY_MOLECULE
import it.unibo.alchemist.model.Dependency.MOVEMENT
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment
import it.unibo.alchemist.rx.model.adapters.ObservableNode
import it.unibo.alchemist.rx.model.adapters.ObservableNode.NodeExtension.asObservableNode
import it.unibo.alchemist.rx.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.rx.model.observation.Observable
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.ObservableSetExtensions.flatMap
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.merge
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.switchMap
import it.unibo.alchemist.rx.model.observation.ObservableMap

/**
 * Simple set of utility function for converting and binding a standard
 * [Reaction] and [Condition] into their reactive counterparts.
 */
object ReactiveBinder {

    /**
     * Converts the given [reaction][origin] into a [ReactiveReactionAdapter] enabling
     * reactivity and observable rescheduling requests. This method takes care of converting
     * every [Condition] of this reaction into an appropriate reactive alternative [ReactiveConditionAdapter].
     *
     * @param origin the original [Reaction] to be converted
     * @param environment the environment in which the node containing this reaction is placed.
     * @return a reactive version of this reaction.
     */
    fun <T> bindAndGetReactiveReaction(
        origin: Reaction<T>,
        environment: ObservableEnvironment<T, *>,
    ): ReactiveReactionAdapter<T> = ReactiveReactionAdapterImpl(origin, environment).apply {
        conditions = origin.conditions.map { it.asReactive(environment, this) }
    }

    /**
     * Converts this [Condition] into a [ReactiveConditionAdapter] by transforming the tuple
     * `(CONTEXT, DEPENDENCY)` into an appropriate [Observable] structure, making sure to maintain
     * the granularity of dependencies from standard Alchemist APIs. In this way, there's no need
     * for the dependency graph to match action's outbound dependencies with conditions inbound
     * dependencies.
     *
     * @param environment the [ObservableEnvironment] where the node contains the enclosing reaction of this condition.
     * @param reaction the [Reaction] containing this condition.
     */
    fun <T> Condition<T>.bindAndGetReactiveCondition(
        environment: ObservableEnvironment<T, *>,
        reaction: Reaction<T>,
    ): ReactiveConditionAdapter<T> {
        val deps = mutableListOf<Observable<*>>()
        val sourceNode = reaction.node.asObservableNode()
        inboundDependencies.forEach { dependency ->
            deps += when (dependency) {
                is Molecule -> wireMolecule(environment, reaction, context, dependency, sourceNode)
                MOVEMENT -> wireMovement(environment, reaction, context, sourceNode)
                EVERY_MOLECULE -> wireEveryMolecule(environment, reaction, context, sourceNode)
                EVERYTHING -> wireEverything(environment, reaction, context, sourceNode)
                else -> error("Unrecognised dependency kind \"$dependency\" for condition $this.")
            }
        }

        return ReactiveConditionAdapterImpl(this, deps, environment)
    }

    private fun <T> wireMolecule(
        environment: ObservableEnvironment<T, *>,
        reaction: Reaction<T>,
        context: Context,
        target: Molecule,
        node: ObservableNode<T>,
    ): Observable<*> = with(node) {
        when (context) {
            Context.LOCAL -> observeConcentration(target)
            Context.NEIGHBORHOOD -> environment.observeNeighborhood(this).switchMap { neighborhood ->
                neighborhood.map {
                    it.add(this).flatMap { n -> n.observeConcentration(target) }
                }.getOrElse { observe(arrow.core.none()) }
            }
            Context.GLOBAL -> environment.nodes.map {
                it.asObservableNode().observeConcentration(target)
            }.merge() // quite wrong, this is a snapshot of current nodes, this set of node should be [ObservableSet]
        }
    }

    private fun <T> wireMovement(
        environment: ObservableEnvironment<T, *>,
        reaction: Reaction<T>,
        context: Context,
        node: ObservableNode<T>,
    ): Observable<*> = when (context) {
        Context.LOCAL -> environment.observeNodePosition(node)
        Context.NEIGHBORHOOD -> environment.observeNeighborhood(node).switchMap { neighborhood ->
            neighborhood.map {
                it.add(reaction.node).flatMap { n -> environment.observeNodePosition(n) }
            }.getOrElse { observe(arrow.core.none()) }
        }
        Context.GLOBAL -> environment.observeAnyMovement()
    }

    private fun <T> wireEveryMolecule(
        environment: ObservableEnvironment<T, *>,
        reaction: Reaction<T>,
        context: Context,
        node: ObservableNode<T>,
    ): Observable<*> = with(node) {
        when (context) {
            Context.LOCAL -> observableContents
            Context.NEIGHBORHOOD -> environment.observeNeighborhood(this).switchMap { neighborhood ->
                neighborhood.map { it.add(this).flatMap { n -> n.observableContents } }.getOrElse {
                    observe(arrow.core.none<ObservableMap<Molecule, T>>())
                }
            }
            Context.GLOBAL -> environment.nodes.map { it.asObservableNode().observableContents }.merge() // see above
        }
    }

    private fun <T> wireEverything(
        environment: ObservableEnvironment<T, *>,
        reaction: Reaction<T>,
        context: Context,
        node: ObservableNode<T>,
    ): Observable<*> = listOf(
        wireEveryMolecule(environment, reaction, context, node),
        wireMovement(environment, reaction, context, node),
    ).merge()
}
