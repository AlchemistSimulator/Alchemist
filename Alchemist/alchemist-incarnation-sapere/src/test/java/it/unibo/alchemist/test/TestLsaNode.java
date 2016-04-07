package it.unibo.alchemist.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Test;

import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
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
        final Environment<List<? extends ILsaMolecule>> env = new Continuous2DEnvironment<>();
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
