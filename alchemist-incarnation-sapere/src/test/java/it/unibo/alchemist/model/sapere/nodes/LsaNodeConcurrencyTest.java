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
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import it.unibo.alchemist.model.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for concurrent access to LsaNode to ensure thread safety.
 */
class LsaNodeConcurrencyTest {

    private static final int MIN_MOLECULES = 5;
    private static final int NUMBER_OF_OPERATIONS = 100;
    private static final int NUMBER_OF_THREADS = 10;
    private static final int TIMEOUT_SECONDS = 30;
    private static final String SECONDS = " seconds";

    @Test
    void testConcurrentGetContentsAndModification() {
        final SAPEREIncarnation<Euclidean2DPosition> incarnation = new SAPEREIncarnation<>();
        final Environment<List<ILsaMolecule>, Euclidean2DPosition> environment = new Continuous2DEnvironment<>(incarnation);
        final LsaNode node = new LsaNode(environment);
        // Add some initial molecules
        for (int i = 0; i < 10; i++) {
            node.setConcentration(new LsaMolecule("molecule" + i));
        }
        final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        final CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS);
        final List<Future<?>> tasks = new ArrayList<>(NUMBER_OF_THREADS);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final int threadId = i;
            tasks.add(executor.submit(() -> runConcurrentTask(node, threadId, latch)));
        }
        awaitTaskCompletion(latch, tasks, executor);
        // Verify the node is still in a valid state
        final Map<Molecule, List<ILsaMolecule>> finalContents = node.getContents();
        assertNotNull(finalContents);
        assertTrue(node.getMoleculeCount() >= 0);
    }

    private Void runConcurrentTask(final LsaNode node, final int threadId, final CountDownLatch latch) {
        try {
            for (int j = 0; j < NUMBER_OF_OPERATIONS; j++) {
                if (threadId % 2 == 0) {
                    // Reader threads - should not throw ConcurrentModificationException
                    final Map<Molecule, List<ILsaMolecule>> contents = node.getContents();
                    assertNotNull(contents);
                    final int moleculeCount = node.getMoleculeCount();
                    assertTrue(moleculeCount >= 0);
                    final List<ILsaMolecule> lsaSpace = node.getLsaSpace();
                    assertNotNull(lsaSpace);
                } else {
                    runWriterIteration(node, threadId, j);
                }
            }
        } finally {
            latch.countDown();
        }
        return null;
    }

    private void runWriterIteration(final LsaNode node, final int threadId, final int operation) {
        final ILsaMolecule newMolecule = new LsaMolecule("thread" + threadId + "x" + operation);
        node.setConcentration(newMolecule);
        // Sometimes remove molecules to simulate real concurrent modification
        if (operation % 10 == 0 && node.getMoleculeCount() > MIN_MOLECULES) {
            try {
                node.removeConcentration(newMolecule);
            } catch (final IllegalStateException e) {
                // Expected if molecule was already removed by another thread
            }
        }
    }

    private void awaitTaskCompletion(
        final CountDownLatch latch,
        final List<Future<?>> tasks,
        final ExecutorService executor
    ) {
        AssertionError primaryFailure = null;
        try {
            try {
                assertTrue(
                    latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "Test should complete within " + TIMEOUT_SECONDS + SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Test interrupted while waiting for tasks", e);
            }
            for (final Future<?> task : tasks) {
                waitForTask(task);
            }
        } catch (final AssertionError e) {
            primaryFailure = e;
        } finally {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    final AssertionError terminationError = new AssertionError(
                        "Executor did not terminate within " + TIMEOUT_SECONDS + SECONDS);
                    if (primaryFailure != null) {
                        primaryFailure.addSuppressed(terminationError);
                    } else {
                        throw terminationError;
                    }
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                final AssertionError terminationError = new AssertionError(
                    "Interrupted while waiting for executor termination", e);
                if (primaryFailure != null) {
                    primaryFailure.addSuppressed(terminationError);
                } else {
                    throw terminationError;
                }
            }
        }
        if (primaryFailure != null) {
            throw primaryFailure;
        }
    }

    private static void waitForTask(final Future<?> task) {
        try {
            task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Test interrupted while waiting for task", e);
        } catch (final ExecutionException e) {
            throw new AssertionError("Task failed with exception", e.getCause());
        } catch (final TimeoutException e) {
            task.cancel(true);
            throw new AssertionError("Task timed out after " + TIMEOUT_SECONDS + SECONDS, e);
        }
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
