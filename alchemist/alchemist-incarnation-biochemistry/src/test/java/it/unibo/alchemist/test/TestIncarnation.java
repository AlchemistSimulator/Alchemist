/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
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

import it.unibo.alchemist.exceptions.BiochemistryParseException;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.actions.AddJunctionInCell;
import it.unibo.alchemist.model.implementations.actions.AddJunctionInNeighbor;
import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInCell;
import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInEnv;
import it.unibo.alchemist.model.implementations.actions.ChangeBiomolConcentrationInNeighbor;
import it.unibo.alchemist.model.implementations.actions.RemoveJunctionInCell;
import it.unibo.alchemist.model.implementations.actions.RemoveJunctionInNeighbor;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInCell;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInEnv;
import it.unibo.alchemist.model.implementations.conditions.BiomolPresentInNeighbor;
import it.unibo.alchemist.model.implementations.conditions.JunctionPresentInCell;
import it.unibo.alchemist.model.implementations.conditions.NeighborhoodPresent;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.CellNodeImpl;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * Test for biochemistry incarnation.
 */
public class TestIncarnation {

    private static final BiochemistryIncarnation<Euclidean2DPosition> INCARNATION = new BiochemistryIncarnation<>();
    private CellNode<Euclidean2DPosition> node;
    private Environment<Double, Euclidean2DPosition> env;
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
        env = new BioRect2DEnvironment();
        node = new CellNodeImpl<>(env);
        rand = new MersenneTwister();
        time = new ExponentialTime<>(1, rand);
    }

    /**
     */
    @Test
    public void testCreateMolecule() {
        makeMol("C");
        makeMol("H");
        makeMol("OH");
        makeMol("abcdef");

        assertEquals(makeMol("Cl"), makeMol("Cl"));
        assertEquals(makeMol("abcdef"), makeMol("abcdef"));
        assertNotEquals(makeMol("X"), makeMol("Y"));
        assertNotEquals(makeMol("abc"), makeMol("Abc"));
    }

    private int count(final List<?> target, final Class<?> clazz) {
        return (int) target.stream().filter(t -> t.getClass().equals(clazz)).count();
    }

    private void testR(final String param, 
            final int nCond, 
            final int nAct, 
            final int nCellCond,
            final int nCellAct,
            final int nNeighCond,
            final int nNeighAct,
            final int nEnvCond, 
            final int nEnvAct) { // TODO custom conditions and actions
        final Reaction<Double> r = INCARNATION.createReaction(rand, env, node, time, param);
        assertNotNull(r);
        assertEquals(nCond, r.getConditions().size());
        assertEquals(nAct, r.getActions().size());
        // conditions
        assertEquals(nCellCond, count(r.getConditions(), BiomolPresentInCell.class)
                + count(r.getConditions(), JunctionPresentInCell.class));
        assertEquals(nNeighCond, count(r.getConditions(), BiomolPresentInNeighbor.class)
                + count(r.getConditions(), NeighborhoodPresent.class));
        assertEquals(nEnvCond, count(r.getConditions(), BiomolPresentInEnv.class));
        // actions
        assertEquals(nCellAct, count(r.getActions(), AddJunctionInCell.class)
                + count(r.getActions(), ChangeBiomolConcentrationInCell.class)
                + count(r.getActions(), RemoveJunctionInCell.class));
        assertEquals(nNeighAct, count(r.getActions(), AddJunctionInNeighbor.class)
                + count(r.getActions(), ChangeBiomolConcentrationInNeighbor.class)
                + count(r.getActions(), RemoveJunctionInNeighbor.class));
        assertEquals(nEnvAct, count(r.getActions(), ChangeBiomolConcentrationInEnv.class));
    }

    private void testNoR(final String param) { // used for cases like [A] + [B in neighbor] --> [junction A-C]
        try {
            INCARNATION.createReaction(rand, env, node, time, param);
            fail();
        } catch (final BiochemistryParseException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    /**
     * Test various flavors of reaction creation.
     */
    @Test
    public void testCreateReaction() {
        //CHECKSTYLE: MagicNumber OFF
        testR("[] --> []", 0, 0, 0, 0, 0, 0, 0, 0);
        testR("[] + [] --> [] + []", 0, 0, 0, 0, 0, 0, 0, 0);
        testR("[A] --> []", 1, 1, 1, 1, 0, 0, 0, 0);
        testR("[] --> [A]", 0, 1, 0, 1, 0, 0, 0, 0);
        testR("[A] --> [B]", 1, 2, 1, 2, 0, 0, 0, 0);
        testR("[A + B] --> []", 2, 2, 2, 2, 0, 0, 0, 0);
        testR("[A + B] + [C + D] --> []", 4, 4, 4, 4, 0, 0, 0, 0);
        testR("[] --> [2 A + 5B]", 0, 2, 0, 2, 0, 0, 0, 0);
        testR("[2A in neighbor] --> []", 1, 1, 0, 0, 1, 1, 0, 0);
        testR("[A + 3B in neighbor] --> []", 2, 2, 0, 0, 2, 2, 0, 0);
        testR("[] --> [A in neighbor]", 1, 1, 0, 0, 1, 1, 0, 0); // neighborhoodPresent condition is present
        testR("[A] + [B in neighbor] --> [C]", 2, 3, 1, 2, 1, 1, 0, 0); // neighborhoodPresent condition is present
        testR("[A] --> [B] + [C + D in neighbor]", 2, 4, 1, 2, 1, 2, 0, 0); // neighborhoodPresent condition is present
        testR("[A in env] --> []", 1, 1, 0, 0, 0, 0, 1, 1);
        testR("[A + 3B in env] --> []", 2, 2, 0, 0, 0, 0, 2, 2);
        testR("[] --> [A in env]", 1, 1, 0, 0, 0, 0, 0, 1);
        testR("[A] + [B in env] --> [C]", 2, 3, 1, 2, 0, 0, 1, 1);
        testR("[A] --> [B] + [C + D in env]", 2, 4, 1, 2, 0, 0, 0, 2);
        testR("[A] + [B + 2C in neighbor] + [D in env] --> [E in cell] + [F + 4G in env]", 4, 7, 1, 2, 2, 2, 1, 3);
        testR("[A in env] + [B in env] + [C + 4D in neighbor] --> [E + F + G]", 4, 7, 0, 3, 2, 2, 2, 2);
        testR("[A] + [B in neighbor] --> [junction A-B]", 2, 4, 1, 2, 1, 2, 0, 0);
        testR("[A + 3B] + [C in neighbor] + [D in env] --> [junction A:3B-C] + [D in env]", 4, 7, 2, 3, 1, 2, 1, 2);
        testR("[junction A-B] + [A in cell] --> [A in env]", 2, 4, 2, 2, 0, 1, 0, 1);
        testR("[A in env] + [B + 2C] + [junction A-B] --> [junction A-B] + [D in neighbor]", 4, 4, 3, 2, 0, 1, 1, 1); // the junction is not removed like a biomolecule if the same junction is present in the right side.
        testR("[junction A-B] + [junction C-D] --> [A in env]", 2, 5, 2, 2, 0, 2, 0, 1); // the junctions will be removed, because they are not present in the right side.
        testR("[junction A-B] --> [junction A-B] + [A in cell]", 1, 1, 1, 1, 0, 0, 0, 0);
        testR("[A + B] --> [BrownianMove(0.1)]", 2, 3, 2, 2, 0, 0, 0, 0);
        testR("[] --> [B in env] if BiomolPresentInCell(A, 2)", 2, 1, 1, 0, 0, 0, 0, 1); // if a custom condition is used the molecules present in the custom condition will NOT be removed.
        testR("[A] + [B in neighbor] + [C in env] --> [D in cell] + [E in neighbor] + [F in env] + [BrownianMove(1)] if BiomolPresentInCell(A, 2)", 4, 7, 2, 2, 1, 2, 1, 2);
        // CHECKSTYLE: MagicNumber ON
        testNoR("[A] + [B in neighbor] --> [junction A-C]"); // C is not present in conditions
        testNoR("[A] + [B in neighbor] --> [junction A-2B]"); // only one molecule B is present in conditions
        testNoR("[A] + [B in neighbor] --> [junction B-A]"); // A is in cell an B is in neighbor. Correct syntax is junction A-B
        testNoR("[A + B] + [C in neighbor] --> [junction AB-C]"); // AB is considered one molecule. Use A:B
        testNoR("[A + 3B] + [C in neighbor] --> [junction 4B-C]"); // only 3 molecules of B can be used for create the junction
        testNoR("[A] + [B in neighbor] + [C in env] --> [junction A-B:C]"); // molecules in environment cannot be included in junctions
        testNoR("[junction A-B] --> [junction C-D]"); // cannot have a new junction in the right side if is present a junction in the left side
        testNoR("[A] + [B in neighbor] + [junction X-Y] --> [junction A-B]"); // cannot create junctions with junctions conditions
        testNoR("[junction A-B] --> [junction B-A]"); // junction A-B != junction B-A
    }

    /**
     * 
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateNode() {
        INCARNATION.createNode(rand, env, "foo");
    }

}
