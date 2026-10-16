/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.NetworkModel
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.networks.CombinedNetworkModel
import it.unibo.alchemist.model.networks.NoLinks
import org.apache.commons.math3.random.RandomGenerator

/**
 * DSL scope for configuring an [Environment] within a simulation scenario.
 *
 * This context groups environment-level configuration concerns, including:
 * - deploying nodes through a [DeploymentsContext];
 * - registering global reactions;
 * - registering layers (molecule → [Layer] mappings);
 * - configuring the environment network model ([NetworkModel]);
 * - registering termination predicates.
 *
 * Most operations rely on Kotlin context receivers: the relevant [Environment] (and, when applicable,
 * the [Incarnation] and/or [RandomGenerator]) must be available in the surrounding scope.
 *
 * @param T the concentration type used by the simulation.
 * @param P the position type used by the environment.
 */
fun interface EnvironmentContext<T, P : Position<P>> {

    /**
     * Enters the node deployment DSL.
     *
     * The provided [deploymentsConfiguration] is executed
     * with a [RandomGenerator] available as a context receiver and with
     * [DeploymentsContext] as receiver, allowing the scenario to create and configure nodes produced by
     * one or more [it.unibo.alchemist.model.Deployment] strategies.
     *
     * Implementations are expected to ensure a consistent usage of random generators for reproducibility.
     *
     * @param deploymentsConfiguration the deployment configuration block.
     */
    fun deployments(deploymentsConfiguration: context(RandomGenerator) DeploymentsContext<T, P>.() -> Unit)

    /**
     * Registers a [Reaction] in the current [Environment] and optionally configures it.
     *
     * The [block], if provided, is executed with [reaction] as a context receiver and with
     * [ActionableContext] as receiver, enabling the addition of actions and conditions.
     *
     * The [timeDistribution] parameter is currently not used by this function. The expectation is that
     * the provided [reaction] is already configured with the intended scheduling strategy (if any),
     * or that the distribution is otherwise bound externally.
     *
     * @param timeDistribution a time distribution associated with the global program; currently ignored.
     * @param reaction the environment-hosted reaction to register.
     * @param block an optional configuration block for actions and conditions.
     */
    context(environment: Environment<T, P>)
    fun globalProgram(
        timeDistribution: TimeDistribution<T>,
        reaction: Reaction<T>,
        block: context(Reaction<T>) ActionableContext.() -> Unit = {},
    ) {
        context(reaction) {
            ActionableContext.block()
        }
        environment.addReaction(reaction)
    }

    /**
     * Registers a [Layer] associated with the given [molecule] in the current [Environment].
     *
     * Layers are used to provide environment-wide values (e.g., fields or maps) that can be queried by nodes
     * or reactions via the associated molecule.
     *
     * @param molecule the molecule acting as key for the layer.
     * @param layer the layer instance to register.
     */
    context(environment: Environment<T, P>)
    fun layer(molecule: Molecule, layer: Layer<T, P>) = environment.addLayer(molecule, layer)

    /**
     * Registers a [Layer] associated with a molecule identified by [molecule] in the current [Environment].
     *
     * The molecule is created via [Incarnation.createMolecule].
     *
     * @param molecule the molecule name.
     * @param layer the layer instance to register.
     */
    context(incarnation: Incarnation<T, P>, environment: Environment<T, P>)
    fun layer(molecule: String, layer: Layer<T, P>) = layer(incarnation.createMolecule(molecule), layer)

    /**
     * Configures (or extends) the environment network model by adding the provided [model].
     *
     * The current network model is updated as follows:
     * - if the environment currently uses [NoLinks], it is replaced with [model];
     * - otherwise, the existing rule and [model] are combined into a new [CombinedNetworkModel].
     *
     * @param model the network model to set or append, bound to the environment.
     */
    context(environment: Environment<T, P>)
    fun networkModel(model: NetworkModel<T>) = when (val currentRule = environment.networkModel) {
        is NoLinks<T> -> environment.networkModel = model
        else -> environment.networkModel = CombinedNetworkModel(listOf(currentRule, model))
    }

    /**
     * Registers a [TerminationPredicate] in the current [Environment].
     *
     * @param terminator the termination predicate to add.
     */
    context(environment: Environment<T, P>)
    fun terminator(terminator: TerminationPredicate<T, P>) = environment.addTerminator(terminator)
}
