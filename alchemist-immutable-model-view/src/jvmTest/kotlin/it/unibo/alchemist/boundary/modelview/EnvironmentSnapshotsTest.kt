/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.boundary.modelview.EnvironmentSnapshots.snapshot
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.genericReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.test.environmentTestReaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnvironmentSnapshotsTest {
    private val environment: Environment<Any, Euclidean2DPosition> =
        simulation2D(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()) {
            environment {
                networkModel(connectWithinDistance(1.5))
                globalProgram(DiracComb(2.0), environmentTestReaction(DiracComb(2.0)))
                deployments {
                    listOf(0.0 to 0.0, 1.0 to 0.0, 5.0 to 5.0).forEachIndexed { index, (x, y) ->
                        deploy(point(x, y)) {
                            if (index == 0) {
                                contents { "x"(3.0) }
                            }
                            withTimeDistribution(DiracComb<Any>(1.0)) {
                                program(genericReaction())
                            }
                        }
                    }
                }
            }
        }.getDefault<Any, Euclidean2DPosition>().environment

    private val ids = environment.nodes.current.map { it.id }

    @Test
    fun `by default, a snapshot holds positions only`() {
        val snapshot = environment.snapshot()
        val positions = listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(5.0, 5.0))
        assertEquals(positions, snapshot.nodes.map { it.coordinates })
        assertTrue(snapshot.nodes.all { it.values.isEmpty() && it.details == null })
        assertNull(snapshot.links)
    }

    @Test
    fun `requested values are carried by the nodes holding them`() {
        val snapshot = environment.snapshot(SnapshotRequest(values = setOf("x")))
        assertEquals(
            listOf(mapOf("x" to ValueSnapshot.of(3.0)), emptyMap(), emptyMap()),
            snapshot.nodes.map { it.values },
        )
    }

    @Test
    fun `only the requested nodes are detailed, with their contents and reactions`() {
        val snapshot = environment.snapshot(SnapshotRequest(detailed = setOf(ids[0])))
        val details = checkNotNull(snapshot.nodes[0].details)
        assertEquals(listOf(ContentSnapshot("x", ValueSnapshot.of(3.0), "Double")), details.contents)
        val reaction = details.reactions.single()
        assertEquals(1.0, reaction.meanDelay)
        assertTrue(reaction.timeDistribution.orEmpty().startsWith("DiracComb"))
        assertTrue(snapshot.nodes.drop(1).all { it.details == null })
    }

    @Test
    fun `links are sent once each, up to the requested number, with their total`() {
        assertEquals(
            LinksSnapshot(listOf(LinkSnapshot(ids[0], ids[1])), total = 1),
            environment.snapshot(SnapshotRequest(maxLinks = 10)).links,
        )
        assertEquals(LinksSnapshot(emptyList(), total = 1), environment.snapshot(SnapshotRequest(maxLinks = 0)).links)
    }

    @Test
    fun `the reactions of the environment are sent only when asked for`() {
        assertNull(environment.snapshot().environmentReactions)
        val snapshot = environment.snapshot(SnapshotRequest(environmentReactions = true))
        val reaction = checkNotNull(snapshot.environmentReactions).single()
        assertEquals("EnvironmentTestReaction", reaction.type)
        assertEquals(0.5, reaction.meanDelay)
    }
}
