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
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.linkingrules.NoLinks
import org.apache.commons.math3.random.RandomGenerator

/**
 * DSL scope for configuring an [Environment] within a simulation scenario.
 *
 * This context groups environment-level configuration concerns, including:
 * - deploying nodes through a [DeploymentsContext];
 * - registering global reactions;
 * - registering layers (molecule → [Layer] mappings);
 * - configuring the environment network model ([LinkingRule]);
 * - registering termination predicates.
 *
 * Most operations rely on Kotlin context receivers: the relevant [Environment] (and, when applicable,
 * the [Incarnation] and/or [RandomGenerator]) must be available in the surrounding scope.
 *
 * @param T the concentration type used by the simulation.
 * @param P the position type used by the environment.
 */
// TODO: Detekt false positive. Remove once Detekt supports context parameters.
@Suppress("UndocumentedPublicFunction")
interface EnvironmentContext<T, P : Position<P>> {

    /**
     * Enters the node deployment DSL.
     *
     * The provided [block] is executed with a [RandomGenerator] available as a context receiver and with
     * [DeploymentsContext] as receiver, allowing the scenario to create and configure nodes produced by
     * one or more [it.unibo.alchemist.model.Deployment] strategies.
     *
     * Implementations are expected to ensure a consistent usage of random generators for reproducibility.
     *
     * @param block the deployment configuration block.
     */
    fun deployments(block: context(RandomGenerator) DeploymentsContext.() -> Unit)

    /**
     * Registers a [GlobalReaction] in the current [Environment] and optionally configures it.
     *
     * The [block], if provided, is executed with [globalReaction] as a context receiver and with
     * [ActionableContext] as receiver, enabling the addition of actions and conditions.
     *
     * The [timeDistribution] parameter is currently not used by this function. The expectation is that
     * the provided [globalReaction] is already configured with the intended scheduling strategy (if any),
     * or that the distribution is otherwise bound externally.
     *
     * @param timeDistribution a time distribution associated with the global program; currently ignored.
     * @param globalReaction the global reaction to register.
     * @param block an optional configuration block for actions and conditions.
     */
    context(environment: Environment<T, P>)
    fun globalProgram(
        timeDistribution: TimeDistribution<T>,
        globalReaction: GlobalReaction<T>,
        block: context(GlobalReaction<T>) ActionableContext.() -> Unit = {},
    ) {
        context(globalReaction) {
            ActionableContext.block()
        }
        environment.addGlobalReaction(globalReaction)
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
     * The molecule is created via [Incarnation.createMolecule]. The meaning of a `null` molecule name, if any,
     * depends on the concrete incarnation.
     *
     * @param molecule the molecule name, possibly `null` (incarnation-dependent semantics).
     * @param layer the layer instance to register.
     */
    context(incarnation: Incarnation<T, P>, environment: Environment<T, P>)
    fun layer(molecule: String? = null, layer: Layer<T, P>) = layer(incarnation.createMolecule(molecule), layer)

    /**
     * Configures (or extends) the environment network model by adding the provided [model].
     *
     * The current linking rule is updated as follows:
     * - if the environment currently uses [NoLinks], it is replaced with [model];
     * - if the environment currently uses [CombinedLinkingRule], [model] is appended to its sub-rules;
     * - otherwise, the existing rule and [model] are combined into a new [CombinedLinkingRule].
     *
     * @param model the linking rule to set or append.
     */
    context(environment: Environment<T, P>)
    fun networkModel(model: LinkingRule<T, P>) = when (val currentRule = environment.linkingRule) {
        is NoLinks<T, P> -> environment.linkingRule = model
        is CombinedLinkingRule<T, P> -> environment.linkingRule = CombinedLinkingRule(currentRule.subRules + model)
        else -> environment.linkingRule = CombinedLinkingRule(listOf(environment.linkingRule, model))
    }

    /**
     * Registers a [TerminationPredicate] in the current [Environment].
     *
     * @param terminator the termination predicate to add.
     */
    context(environment: Environment<T, P>)
    fun terminator(terminator: TerminationPredicate<T, P>) = environment.addTerminator(terminator)
}
