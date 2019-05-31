/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;

/**
 *
 */
public class TestLsaNode {

    private static final int THREADS = 1000;

    /**
     * 
     */
    @Test
    public void testConcurrentAccess() {
        final Environment<List<ILsaMolecule>, Euclidean2DPosition> env = new Continuous2DEnvironment<>();
        final LsaNode node = new LsaNode(env);
        final CountDownLatch cd = new CountDownLatch(THREADS);
        final Queue<Exception> queue = new ConcurrentLinkedQueue<>();
        final ExecutorService ex = Executors.newCachedThreadPool();
        IntStream.range(0, THREADS).forEach(i -> {
            ex.submit(() -> {
                try {
                    final ILsaMolecule m = new LsaMolecule(Integer.toString(i));
                    cd.countDown();
                    try {
                        cd.await();
                    } catch (Exception e) {
                        queue.add(e);
                    }
                    node.setConcentration(m);
                    node.getContents().forEach((a, b) -> {
                        try {
                            assertTrue(node.contains(a));
                        } catch (Exception e) {
                            queue.add(e);
                        }
                    });
                } catch (ConcurrentModificationException e) {
                    queue.add(e);
                }
            });
        });
        ex.shutdown();
        try {
            ex.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }
        if (!queue.isEmpty()) {
            queue.peek().printStackTrace();
            fail(queue.size() + " concurrent errors detected.");
        }
    }

}
