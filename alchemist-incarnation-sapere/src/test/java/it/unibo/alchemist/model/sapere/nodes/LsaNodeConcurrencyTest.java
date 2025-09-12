/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.nodes;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.SAPEREIncarnation;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import it.unibo.alchemist.model.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for concurrent access to LsaNode to ensure thread safety.
 */
class LsaNodeConcurrencyTest {

    @Test
    void testConcurrentGetContentsAndModification() throws InterruptedException {
        final SAPEREIncarnation<Euclidean2DPosition> incarnation = new SAPEREIncarnation<>();
        final Environment<List<ILsaMolecule>, Euclidean2DPosition> environment = new Continuous2DEnvironment<>(incarnation);
        final LsaNode node = new LsaNode(environment);
        final ILsaMolecule testMolecule = new LsaMolecule("test");
        
        // Add some initial molecules
        for (int i = 0; i < 10; i++) {
            node.setConcentration(new LsaMolecule("molecule" + i));
        }
        
        final int numberOfThreads = 10;
        final int numberOfOperations = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);
        final AtomicBoolean exceptionOccurred = new AtomicBoolean(false);
        
        // Start threads that modify the node while others read from it
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < numberOfOperations; j++) {
                        if (threadId % 2 == 0) {
                            // Reader threads - should not throw ConcurrentModificationException
                            final Map<Molecule, List<ILsaMolecule>> contents = node.getContents();
                            assertNotNull(contents);
                            final int moleculeCount = node.getMoleculeCount();
                            assertTrue(moleculeCount >= 0);
                            final List<ILsaMolecule> lsaSpace = node.getLsaSpace();
                            assertNotNull(lsaSpace);
                        } else {
                            // Writer threads
                            final ILsaMolecule newMolecule = new LsaMolecule("thread" + threadId + "_" + j);
                            node.setConcentration(newMolecule);
                            // Sometimes remove molecules to simulate real concurrent modification
                            if (j % 10 == 0 && node.getMoleculeCount() > 5) {
                                try {
                                    node.removeConcentration(newMolecule);
                                } catch (IllegalStateException e) {
                                    // Expected if molecule was already removed by another thread
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    exceptionOccurred.set(true);
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Test should complete within 30 seconds");
        executor.shutdown();
        
        // No exceptions should have occurred (especially no ConcurrentModificationException)
        assertFalse(exceptionOccurred.get(), "No exceptions should occur during concurrent access");
        
        // Verify the node is still in a valid state
        final Map<Molecule, List<ILsaMolecule>> finalContents = node.getContents();
        assertNotNull(finalContents);
        assertTrue(node.getMoleculeCount() >= 0);
    }
    
    @Test
    void testBasicFunctionalityPreserved() {
        final SAPEREIncarnation<Euclidean2DPosition> incarnation = new SAPEREIncarnation<>();
        final Environment<List<ILsaMolecule>, Euclidean2DPosition> environment = new Continuous2DEnvironment<>(incarnation);
        final LsaNode node = new LsaNode(environment);
        final ILsaMolecule testMolecule = new LsaMolecule("test");
        
        // Test basic operations still work correctly
        assertEquals(0, node.getMoleculeCount());
        assertFalse(node.contains(testMolecule));
        
        node.setConcentration(testMolecule);
        assertEquals(1, node.getMoleculeCount());
        assertTrue(node.contains(testMolecule));
        
        final Map<Molecule, List<ILsaMolecule>> contents = node.getContents();
        assertNotNull(contents);
        assertEquals(1, contents.size());
        
        final List<ILsaMolecule> lsaSpace = node.getLsaSpace();
        assertNotNull(lsaSpace);
        assertEquals(1, lsaSpace.size());
        
        assertTrue(node.removeConcentration(testMolecule));
        assertEquals(0, node.getMoleculeCount());
        assertFalse(node.contains(testMolecule));
    }
}