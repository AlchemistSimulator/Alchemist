/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.actions

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.maps.environments.OSMEnvironment
import it.unibo.alchemist.model.maps.positions.LatLongPosition
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.timedistributions.DiracComb
import org.apache.commons.math3.random.MersenneTwister
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TestTargetMapWalker {
    private lateinit var environment: OSMEnvironment<Any>
    private lateinit var node: Node<Any>
    private lateinit var reaction: Reaction<Any>

    @BeforeEach
    fun `Set up environment and node`() {
        environment = OSMEnvironment(INCARNATION, TESTMAP, true, true).apply { linkingRule = NoLinks() }
        node = INCARNATION.createNode(MersenneTwister(), environment, null)
        reaction = Event(node, DiracComb(1.0))
        val walker = TargetMapWalker(environment, node, reaction, TRACK, INTERACTING)
        reaction.actions = listOf(walker)
        node.addReaction(reaction)
        environment.addNode(node, STARTPOSITION)
    }

    private fun run() = repeat(STEPS) {
        reaction.execute()
        reaction.update(reaction.tau, true, environment)
    }

    @Test
    fun `Node should not move if no position is set`() {
        val start = environment.getPosition(node)
        assertTrue(STARTPOSITION.distanceTo(start) < 10)
        run()
        assertEquals(start, environment.getPosition(node))
    }

    @Test
    fun `Node should reach the target position when LatLongPosition is set`() {
        val start = environment.getPosition(node)
        assertTrue(STARTPOSITION.distanceTo(start) < 10)
        node.setConcentration(TRACK, LatLongPosition(ENDLAT, ENDLON))
        run()
        assertEquals(ENDPOSITION, environment.getPosition(node))
    }

    @Test
    fun `Node should reach the target position when position is set as Iterable of Doubles`() {
        val start = environment.getPosition(node)
        assertTrue(STARTPOSITION.distanceTo(start) < 10)
        node.setConcentration(TRACK, listOf(ENDLAT, ENDLON))
        run()
        assertEquals(ENDPOSITION, environment.getPosition(node))
    }

    @Test
    fun `Node should reach the target position when position is set as Iterable of Strings`() {
        val start = environment.getPosition(node)
        assertTrue(STARTPOSITION.distanceTo(start) < 10)
        node.setConcentration(TRACK, listOf(ENDLAT.toString(), ENDLON.toString()))
        run()
        assertEquals(ENDPOSITION, environment.getPosition(node))
    }

    @Test
    fun `Node should reach the target position when position is set as a stringified list of numbers`() {
        val start = environment.getPosition(node)
        assertTrue(STARTPOSITION.distanceTo(start) < 10)
        node.setConcentration(TRACK, listOf(ENDLAT, ENDLON).toString())
        run()
        assertEquals(ENDPOSITION, environment.getPosition(node))
    }

    @Test
    fun `Node should reach the target position when position is set as a stringified GeoPosition`() {
        val start = environment.getPosition(node)
        assertTrue(STARTPOSITION.distanceTo(start) < 10)
        node.setConcentration(TRACK, ENDPOSITION.toString())
        run()
        assertEquals(ENDPOSITION, environment.getPosition(node))
    }

    @Test
    fun `Node should reach the target position when position is set as an angle bracket string`() {
        val start = environment.getPosition(node)
        assertTrue(STARTPOSITION.distanceTo(start) < 10)
        node.setConcentration(TRACK, "<$ENDLAT $ENDLON>")
        run()
        assertEquals(ENDPOSITION, environment.getPosition(node))
    }

    @Test
    fun `Node should reach the target position when position is set as a string with embedded coordinates`() {
        val start = environment.getPosition(node)
        assertTrue(STARTPOSITION.distanceTo(start) < 10)
        node.setConcentration(TRACK, "sakldaskld$ENDLAT fmekfjr$ENDLON sdsad32d")
        run()
        assertEquals(ENDPOSITION, environment.getPosition(node))
    }

    companion object {
        private val INCARNATION: Incarnation<Any, GeoPosition?> =
            SupportedIncarnations.get<Any, GeoPosition?>("protelis").orElseThrow()
        private const val TESTMAP = "maps/cesena.pbf"
        private val TRACK = SimpleMolecule("track")
        private val INTERACTING = SimpleMolecule("interacting")
        private const val STEPS = 2000
        private const val STARTLAT = 44.13581
        private const val STARTLON = 12.2403
        private const val ENDLAT = 44.143493
        private const val ENDLON = 12.260879

        /*
         * Rocca Malatestiana
         */
        private val STARTPOSITION = LatLongPosition(STARTLAT, STARTLON)

        /*
         * Near Montefiore
         */
        private val ENDPOSITION = LatLongPosition(ENDLAT, ENDLON)
    }
}
