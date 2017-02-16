package it.unibo.alchemist.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks;
import it.unibo.alchemist.model.implementations.nodes.GenericNode;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;

/**
 *
 */
public final class TestContinuous2DEnvironment {

    private static final double [] ZEROS = new double[]{0, 0};
    private static final double [] P2_3 = new double[]{2, 3};
    private static final double [] P2_2 = new double[]{2, 2};
    private static final double TOLERANCE = 1E-15;
    private Continuous2DEnvironment<Object> env = new Continuous2DEnvironment<>();

    /**
     * Instances the environment.
     */
    @Before
    public void setUp() {
        env = new Continuous2DEnvironment<>();
        env.setLinkingRule(new NoLinks<>());
    }

    /**
     * Test size initialization and change.
     */
    @Test
    public void testEnvironmentSize() {
        assertArrayEquals(ZEROS, env.getSize(), TOLERANCE);
        env.addNode(dummyNode(), new Continuous2DEuclidean(P2_3));
        assertArrayEquals(ZEROS, env.getSize(), TOLERANCE);
        env.addNode(dummyNode(), new Continuous2DEuclidean(P2_2));
        assertArrayEquals(new double[]{0, 1}, env.getSize(), TOLERANCE);
        env.addNode(dummyNode(), new Continuous2DEuclidean(ZEROS));
        assertArrayEquals(P2_3, env.getSize(), TOLERANCE);
    }

    /**
     * Test environment offset.
     */
    @Test
    public void testEnvironmentOffset() {
        assertTrue(Double.isNaN(env.getOffset()[0]));
        assertTrue(Double.isNaN(env.getOffset()[1]));
        env.addNode(dummyNode(), new Continuous2DEuclidean(P2_3));
        assertArrayEquals(P2_3, env.getOffset(), TOLERANCE);
        env.addNode(dummyNode(), new Continuous2DEuclidean(P2_2));
        assertArrayEquals(P2_2, env.getOffset(), TOLERANCE);
        env.addNode(dummyNode(), new Continuous2DEuclidean(ZEROS));
        assertArrayEquals(ZEROS, env.getOffset(), TOLERANCE);
    }

    private Node<Object> dummyNode() {
        return dummyNode(env);
    }

    private static Node<Object> dummyNode(final Environment<Object> env) {
        return new GenericNode<Object>(env) {
            private static final long serialVersionUID = 1L;
            @Override
            protected Object createT() {
                return 0;
            }
        };
    }

}
