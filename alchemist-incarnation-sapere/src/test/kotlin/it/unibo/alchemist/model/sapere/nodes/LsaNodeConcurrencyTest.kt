/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.nodes

import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LsaNodeConcurrencyTest {
    private fun newNode(): LsaNode = LsaNode(Continuous2DEnvironment(SAPEREIncarnation<Euclidean2DPosition>()))

    @Test
    fun `concurrent reads and modifications leave the node consistent`() {
        val node = newNode()
        repeat(INITIAL_MOLECULES) { node.setConcentration(LsaMolecule("molecule$it")) }
        val executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
        try {
            val tasks = (0 until NUMBER_OF_THREADS).map { threadId ->
                executor.submit {
                    repeat(NUMBER_OF_OPERATIONS) { operation ->
                        if (threadId % 2 == 0) {
                            // Readers must never observe a ConcurrentModificationException.
                            node.contents
                            node.lsaSpace
                            assertTrue(node.moleculeCount >= 0)
                        } else {
                            write(node, threadId, operation)
                        }
                    }
                }
            }
            // Any failure inside a task is rethrown here, wrapped in an ExecutionException.
            tasks.forEach { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS))
        }
        assertTrue(node.moleculeCount >= 0)
    }

    private fun write(node: LsaNode, threadId: Int, operation: Int) {
        val molecule: ILsaMolecule = LsaMolecule("thread${threadId}x$operation")
        node.setConcentration(molecule)
        // Occasionally remove molecules to simulate concurrent modifications.
        if (operation % REMOVAL_PERIOD == 0 && node.moleculeCount > MIN_MOLECULES) {
            // Another thread may have already removed the molecule.
            runCatching { node.removeConcentration(molecule) }
                .onFailure { check(it is IllegalStateException) { "Unexpected failure: $it" } }
        }
    }

    @Test
    fun `basic node operations work`() {
        val node = newNode()
        val molecule: ILsaMolecule = LsaMolecule("test")
        assertEquals(0, node.moleculeCount)
        assertFalse(node.contains(molecule))
        node.setConcentration(molecule)
        assertEquals(1, node.moleculeCount)
        assertTrue(node.contains(molecule))
        assertEquals(1, node.contents.size)
        assertEquals(1, node.lsaSpace.size)
        assertTrue(node.removeConcentration(molecule))
        assertEquals(0, node.moleculeCount)
        assertFalse(node.contains(molecule))
    }

    private companion object {
        private const val INITIAL_MOLECULES = 10
        private const val MIN_MOLECULES = 5
        private const val REMOVAL_PERIOD = 10
        private const val NUMBER_OF_OPERATIONS = 100
        private const val NUMBER_OF_THREADS = 10
        private const val TIMEOUT_SECONDS = 30L
    }
}
