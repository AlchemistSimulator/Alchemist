/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.GenericReaction
import it.unibo.alchemist.model.sapere.actions.LsaAllNeighborsAction
import it.unibo.alchemist.model.sapere.actions.LsaRandomNeighborAction
import it.unibo.alchemist.model.sapere.conditions.LsaNeighborhoodCondition
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

class TestIncarnation {
    private val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
    private val environment: Environment<List<ILsaMolecule>, Euclidean2DPosition> = Continuous2DEnvironment(incarnation)
    private val node: ILsaNode = LsaNode(environment)
    private val randomGenerator: RandomGenerator = MersenneTwister()
    private val timeDistribution: TimeDistribution<List<ILsaMolecule>> = SAPEREExponentialTime("1", randomGenerator)

    private fun makeMolecule(specification: String, arguments: Int, ground: Boolean): ILsaMolecule =
        incarnation.createMolecule(specification).also {
            assertEquals(ground, it.isIstance)
            assertEquals(arguments, it.argsNumber())
            assertEquals(arguments, it.size())
        }

    @Test
    fun `molecules are parsed with their arguments`() {
        makeMolecule("  source, Distance  ", 2, false)
        makeMolecule("gradient,   Distance, #O", 3, false)
        makeMolecule("gradient, Distance, Dest", 3, false)
        makeMolecule("context, K", 2, false)
        makeMolecule("walker", 1, true)
        makeMolecule("sub, 0.9", 2, true)
        makeMolecule("{sub, 0.9}", 2, true)
        assertEquals(makeMolecule("sub, 0.9", 2, true), makeMolecule("{sub, 0.9}", 2, true))
    }

    private fun assertTimeDistribution(parameter: String?, rate: Double, occurrence: Double) {
        val reaction = GenericReaction(
            node,
            incarnation.createTimeDistribution(randomGenerator, environment, node, parameter),
        )
        if (!rate.isNaN()) {
            assertEquals(rate, reaction.rate, Double.MIN_VALUE)
        }
        if (!occurrence.isNaN()) {
            assertEquals(occurrence, reaction.nextOccurrence.current.toDouble(), Double.MIN_VALUE)
        }
    }

    @Test
    fun `time distributions are created from rate expressions and start times`() {
        assertTimeDistribution(null, Double.POSITIVE_INFINITY, 0.0)
        assertTimeDistribution("", Double.POSITIVE_INFINITY, 0.0)
        assertTimeDistribution("Infinity", Double.POSITIVE_INFINITY, 0.0)
        assertTimeDistribution("10", 10.0, Double.NaN)
        assertTimeDistribution("10 * 10", 100.0, Double.NaN)
        assertTimeDistribution("Infinity, 100", Double.POSITIVE_INFINITY, 100.0)
        assertTimeDistribution("N * 3", Double.NaN, Double.NaN)
    }

    private fun assertReaction(
        parameter: String?,
        conditions: Int,
        actions: Int,
        neighborConditions: Int = 0,
        neighborActions: Int = 0,
        allNeighborsActions: Int = 0,
    ) {
        val reaction = incarnation.createReaction(randomGenerator, environment, node, timeDistribution, parameter)
        assertEquals(conditions, reaction.conditions.size)
        assertEquals(actions, reaction.actions.size)
        assertEquals(neighborConditions, reaction.conditions.count { it::class == LsaNeighborhoodCondition::class })
        assertEquals(neighborActions, reaction.actions.count { it::class == LsaRandomNeighborAction::class })
        assertEquals(allNeighborsActions, reaction.actions.count { it::class == LsaAllNeighborsAction::class })
    }

    private fun assertNoReaction(parameter: String) {
        val error = assertFailsWith<IllegalArgumentException> {
            incarnation.createReaction(randomGenerator, environment, node, timeDistribution, parameter)
        }
        assertFalse(error.message.isNullOrEmpty())
    }

    @Test
    fun `reactions are created from their specification`() {
        assertReaction(null, 0, 0)
        assertReaction("", 0, 0)
        assertReaction("-->", 0, 0)
        assertReaction("{token} -->", 1, 0)
        assertReaction("--> {token}", 0, 1)
        assertReaction("{token, N} -->", 1, 0)
        assertReaction("{token}{token} -->", 2, 0)
        assertReaction("{token} {token} -->", 2, 0)
        assertReaction("{token}   {token} -->", 2, 0)
        assertReaction("{token}+{token} -->", 2, 0, neighborConditions = 1)
        assertReaction("{token} +{token} -->", 2, 0, neighborConditions = 1)
        assertReaction("{token, N} +{token, N} --> {test, N}", 2, 1, neighborConditions = 1)
        assertReaction("{token, N} +{token, N} --> +{token, N}", 2, 1, neighborConditions = 1, neighborActions = 1)
        assertReaction("{token, N} +{token, N} --> {token, N}", 2, 1, neighborConditions = 1)
        assertReaction("{token, N} +{token, N} --> *{token, N}", 2, 1, neighborConditions = 1, allNeighborsActions = 1)
    }

    @Test
    fun `malformed reaction specifications are rejected`() {
        listOf(
            "asdsad",
            "asdsad {}",
            "{} --> ",
            "--> {}",
            "{} --> {}",
            "{a} --> {}",
            "{} --> {a}",
            "a {a} --> {a}",
            "{a} a--> {a}",
            "{a} a {a}--> {a}",
            "--> a {a}",
            "--> a {a} a",
            "--> {a} a",
            "->",
            "aasda",
            "--->",
        ).forEach(::assertNoReaction)
    }
}
