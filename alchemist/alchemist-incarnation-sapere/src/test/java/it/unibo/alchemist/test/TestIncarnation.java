/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.alchemist.model.SAPEREIncarnation;
import it.unibo.alchemist.model.implementations.actions.LsaAllNeighborsAction;
import it.unibo.alchemist.model.implementations.actions.LsaRandomNeighborAction;
import it.unibo.alchemist.model.implementations.conditions.LsaNeighborhoodCondition;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.timedistributions.SAPEREExponentialTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * Test for the SAPERE Incarnation.
 */
public final class TestIncarnation {

    private final SAPEREIncarnation<Euclidean2DPosition> incarnation = new SAPEREIncarnation<>();
    private ILsaNode node;
    private Environment<List<ILsaMolecule>, Euclidean2DPosition> env;
    private RandomGenerator rand;
    private TimeDistribution<List<ILsaMolecule>> time;

    private ILsaMolecule mkMol(final String s, final int args, final boolean ground) {
        final ILsaMolecule res = incarnation.createMolecule(s);
        assertNotNull(res);
        assertEquals(ground, res.isIstance());
        assertEquals(args, res.argsNumber());
        assertEquals(args, res.size());
        return res;
    }

    /**
     * 
     */
    @BeforeEach
    public void setUp() {
        env = new Continuous2DEnvironment<>();
        node = new LsaNode(env);
        rand = new MersenneTwister();
        time = new SAPEREExponentialTime("1", rand);
    }

    /**
     * Test molecule creation.
     */
    @Test
    public void testCreateMolecule() {
        mkMol("  source, Distance  ", 2, false);
        mkMol("gradient,   Distance, #O", 3, false);
        mkMol("gradient, Distance, Dest", 3, false);
        mkMol("context, K", 2, false);
        mkMol("walker", 1, true);
        mkMol("sub, 0.9", 2, true);
        mkMol("{sub, 0.9}", 2, true);
        assertEquals(mkMol("sub, 0.9", 2, true), mkMol("{sub, 0.9}", 2, true));
    }

    private void testTD(final String param, final double rate, final double occurrence) {
        final TimeDistribution<List<ILsaMolecule>> t0 = incarnation.createTimeDistribution(rand, env, node, param);
        assertNotNull(t0);
        if (!Double.isNaN(rate)) {
            assertEquals(rate, t0.getRate(), Double.MIN_VALUE);
        }
        if (!Double.isNaN(occurrence)) {
            assertEquals(occurrence, t0.getNextOccurence().toDouble(), Double.MIN_VALUE);
        }
    }

    /**
     * Test the creation of various time distributions.
     */
    @Test
    public void testCreateTimeDistribution() {
        testTD(null, Double.POSITIVE_INFINITY, 0);
        testTD("", Double.POSITIVE_INFINITY, 0);
        testTD("Infinity", Double.POSITIVE_INFINITY, 0);
        testTD("10", 10, Double.NaN);
        testTD("10 * 10", 100, Double.NaN);
        testTD("Infinity, 100", Double.POSITIVE_INFINITY, 100);
        testTD("N * 3", Double.NaN, Double.NaN);
    }

    private static int count(final List<?> target, final Class<?> clazz) {
        return (int) target.stream().filter(o -> clazz.equals(o.getClass())).count();
    }

    private void testR(final String param, final int ncond, final int nact, final int nneighcond, final int nneighact, final int nallneighact) {
        final Reaction<List<ILsaMolecule>> r = incarnation.createReaction(rand, env, node, time, param);
        assertNotNull(r);
        assertEquals(ncond, r.getConditions().size());
        assertEquals(nact, r.getActions().size());
        assertEquals(nneighcond, count(r.getConditions(), LsaNeighborhoodCondition.class));
        assertEquals(nneighact, count(r.getActions(), LsaRandomNeighborAction.class));
        assertEquals(nallneighact, count(r.getActions(), LsaAllNeighborsAction.class));
    }

    private void testNoR(final String param) {
        try {
            incarnation.createReaction(rand, env, node, time, param);
            fail();
        } catch (IllegalArgumentException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    /**
     * Test various flavors of reaction creation.
     */
    @Test
    public void testCreateReaction() {
        testR(null, 0, 0, 0, 0, 0);
        testR("", 0, 0, 0, 0, 0);
        testR("-->", 0, 0, 0, 0, 0);
        testR("{token} -->", 1, 0, 0, 0, 0);
        testR("--> {token}", 0, 1, 0, 0, 0);
        testR("{token, N} -->", 1, 0, 0, 0, 0);
        testR("{token}{token} -->", 2, 0, 0, 0, 0);
        testR("{token} {token} -->", 2, 0, 0, 0, 0);
        testR("{token}   {token} -->", 2, 0, 0, 0, 0);
        testR("{token}+{token} -->", 2, 0, 1, 0, 0);
        testR("{token} +{token} -->", 2, 0, 1, 0, 0);
        testR("{token, N} +{token, N} --> {test, N}", 2, 1, 1, 0, 0);
        testR("{token, N} +{token, N} --> +{token, N}", 2, 1, 1, 1, 0);
        testR("{token, N} +{token, N} --> {token, N}", 2, 1, 1, 0, 0);
        testR("{token, N} +{token, N} --> *{token, N}", 2, 1, 1, 0, 1);
        testNoR("asdsad");
        testNoR("asdsad {}");
        testNoR("{} --> ");
        testNoR("--> {}");
        testNoR("{} --> {}");
        testNoR("{a} --> {}");
        testNoR("{} --> {a}");
        testNoR("{} --> {a}");
        testNoR("a {a} --> {a}");
        testNoR("{a} a--> {a}");
        testNoR("{a} a {a}--> {a}");
        testNoR("--> a {a}");
        testNoR("--> a {a} a");
        testNoR("--> {a} a");
        testNoR("->");
        testNoR("aasda");
        testNoR("--->");
    }

}
