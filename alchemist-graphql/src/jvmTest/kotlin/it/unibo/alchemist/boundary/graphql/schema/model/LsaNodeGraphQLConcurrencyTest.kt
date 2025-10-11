/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model

import it.unibo.alchemist.boundary.GraphQLTestEnvironments
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLNodeSurrogate
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

/**
 * Tests that verify the fix for race conditions in LsaNode when accessed via GraphQL.
 * This reproduces the original ConcurrentModificationException that occurred when
 * GraphQL subscriptions accessed node contents while simulation reactions modified the same nodes.
 */
class LsaNodeGraphQLConcurrencyTest<T, P> where T : Any, P : Position<P>, P : Vector<P> {

    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    fun `GraphQL node contents access should not cause ConcurrentModificationException during simulation`() {
        GraphQLTestEnvironments.loadTests<T, P> { environment ->
            val nodes = environment.nodes.filterIsInstance<LsaNode>()
            // Skip test if no SAPERE nodes are available
            check(nodes.isNotEmpty())
            val threadCount = 20
            val operationsPerThread = 500
            val latch = CountDownLatch(threadCount * 2) // Double for both reader and writer threads
            val exceptions = AtomicInteger(0)
            val successfulReads = AtomicInteger(0)
            val successfulWrites = AtomicInteger(0)
            // Start reader threads that simulate GraphQL subscription access
            repeat(threadCount) {
                Thread {
                    try {
                        repeat(operationsPerThread) {
                            // This is what GraphQL does when accessing node contents
                            val randomNode = nodes.random()
                            val nodeSurrogate = randomNode.toGraphQLNodeSurrogate()
                            // This calls LsaNode.getContents() which was causing ConcurrentModificationException
                            val contents = nodeSurrogate.contents()
                            // Verify we can access the size safely
                            contents.size
                            successfulReads.incrementAndGet()
                        }
                    } catch (e: ConcurrentModificationException) {
                        exceptions.incrementAndGet()
                        throw e
                    } catch (e: Exception) {
                        // Other exceptions are acceptable in this test context
                        println("Reader thread caught non-CME exception: ${e::class.simpleName}")
                    } finally {
                        latch.countDown()
                    }
                }.start()
            }
            // Start writer threads that simulate simulation reactions modifying nodes
            repeat(threadCount) {
                Thread {
                    try {
                        repeat(operationsPerThread) {
                            // Simulate reactions modifying node contents
                            val randomNode = nodes.random()
                            // This modifies the instances collection, potentially causing race conditions
                            try {
                                val firstMolecule = randomNode.lsaSpace.firstOrNull()
                                if (firstMolecule != null) {
                                    randomNode.setConcentration(firstMolecule)
                                }
                            } catch (e: Exception) {
                                // Expected in some cases during concurrent access
                                println("Writer operation caught exception: ${e::class.simpleName}")
                            }
                            successfulWrites.incrementAndGet()
                        }
                    } catch (e: Exception) {
                        // Exceptions during writes are acceptable for this test
                        println("Writer thread caught exception: ${e::class.simpleName}")
                    } finally {
                        latch.countDown()
                    }
                }.start()
            }
            // Wait for all threads to complete
            assertTrue(latch.await(90, TimeUnit.SECONDS), "Test should complete within timeout")
            // The key assertion: no ConcurrentModificationException should occur
            assertEquals(0, exceptions.get(), "No ConcurrentModificationException should occur with the fix")
            // Verify that operations actually happened
            assertTrue(successfulReads.get() > 0, "Should have successful reads")
            assertTrue(successfulWrites.get() > 0, "Should have successful writes")
            println("Test completed successfully:")
            println("- Successful reads: ${successfulReads.get()}")
            println("- Successful writes: ${successfulWrites.get()}")
            println("- ConcurrentModificationExceptions: ${exceptions.get()}")
        }
    }
}
