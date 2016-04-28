package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;

import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.actions.AddJunctionInCell;
import it.unibo.alchemist.model.implementations.actions.AddJunctionInEnv;
import it.unibo.alchemist.model.implementations.actions.AddJunctionInNeighbor;
import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInCell;
import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInEnv;
import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInNeighbor;
import it.unibo.alchemist.model.implementations.actions.RemoveJunctionInCell;
import it.unibo.alchemist.model.implementations.actions.RemoveJunctionInEnv;
import it.unibo.alchemist.model.implementations.actions.RemoveJunctionInNeighbor;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInCell;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInEnv;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInNeighbor;
import it.unibo.alchemist.model.implementations.conditions.JunctionPresentInCell;
import it.unibo.alchemist.model.implementations.conditions.JunctionPresentInEnv;
import it.unibo.alchemist.model.implementations.conditions.JunctionPresentInNeighbor;
import it.unibo.alchemist.model.implementations.environments.Rect2DEnvironment;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNode;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ICellNode;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * Test for biochemistry incarnation.
 */
public class TestIncarnation {

    private static final BiochemistryIncarnation INCARNATION = new BiochemistryIncarnation();
    private ICellNode node;
    private Environment<Double> env;
    private RandomGenerator rand;
    private TimeDistribution<Double> time;

    private static Biomolecule makeMol(final String name) {
        final Biomolecule biomol = INCARNATION.createMolecule(name);
        assertNotNull(biomol);
        return biomol;
    }

    /**
     */
    @Before
    public void setUp() {
        env = new Rect2DEnvironment();
        node = new CellNode(env);
        rand = new MersenneTwister();
        time = new ExponentialTime<>(1, rand);
    }

    /**
     */
    @Test
    public void testCreateMolecule() {
        makeMol("C");
        makeMol("H");
        makeMol("O");

        assertEquals(makeMol("Cl"), makeMol("Cl"));
        assertEquals(makeMol("asdfghjkl"), makeMol("asdfghjkl"));
        assertNotEquals(makeMol("X"), makeMol("Y"));
        assertNotEquals(makeMol("asdfghjkl"), makeMol("Asdfghjkl"));
    }


    private int count(final List<?> target, final Class<?> clazz) {
        return (int) target.stream().filter(t -> toString().getClass().equals(clazz)).count();
    }

    private void testR(final String param, 
            final int nCond, 
            final int nAct, 
            final int nCellCond,
            final int nCellAct,
            final int nNeighCond,
            final int nNeighAct,
            final int nEnvCond, 
            final int nEnvAct) {
        final Reaction<Double> r = INCARNATION.createReaction(rand, env, node, time, param);
        assertNotNull(r);
        assertEquals(nCond, r.getConditions().size());
        assertEquals(nAct, r.getActions().size());
        // conditions
        assertEquals(nCellCond, count(r.getConditions(), BiomolPresentInCell.class)
                + count(r.getConditions(), JunctionPresentInCell.class));
        assertEquals(nNeighCond, count(r.getConditions(), BiomolPresentInNeighbor.class)
                + count(r.getConditions(), JunctionPresentInNeighbor.class));
        assertEquals(nEnvCond, count(r.getConditions(), BiomolPresentInEnv.class)
                + count(r.getConditions(), JunctionPresentInEnv.class));
        // actions
        assertEquals(nCellAct, count(r.getActions(), AddJunctionInCell.class)
                + count(r.getActions(), ChangeBiomolConcentrationInCell.class)
                + count(r.getActions(), RemoveJunctionInCell.class));
        assertEquals(nNeighAct, count(r.getActions(), AddJunctionInNeighbor.class)
                + count(r.getActions(), ChangeBiomolConcentrationInNeighbor.class)
                + count(r.getActions(), RemoveJunctionInNeighbor.class));
        assertEquals(nEnvAct, count(r.getActions(), AddJunctionInEnv.class)
                + count(r.getActions(), ChangeBiomolConcentrationInEnv.class)
                + count(r.getActions(), RemoveJunctionInEnv.class));
    }

    private void testNoR(final String param) {
        try {
            INCARNATION.createReaction(rand, env, node, time, param);
            fail();
        } catch (final IllegalArgumentException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    /**
     * Test various flavors of reaction creation.
     */
    @Test
    public void testCreateReaction() {
        testR(null, 0, 0, 0, 0, 0, 0, 0, 0);
        /*
         * TODO
         */
    }

}
