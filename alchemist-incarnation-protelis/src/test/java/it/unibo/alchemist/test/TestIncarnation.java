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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import it.unibo.alchemist.model.interfaces.Reaction;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import it.unibo.alchemist.model.ProtelisIncarnation;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.actions.SendToNeighbor;
import it.unibo.alchemist.model.implementations.conditions.ComputationalRoundComplete;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.reactions.ChemicalReaction;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.TimeDistribution;


/**
 */
class TestIncarnation {

    private static final ProtelisIncarnation<Euclidean2DPosition> INCARNATION = new ProtelisIncarnation<>();

    /**
     * Tests the ability of {@link ProtelisIncarnation} of properly building a
     * Alchemist entities for running Protelis.
     */
    @Test
    void testBuild() {
        final RandomGenerator rng = new MersenneTwister(0);
        final Environment<Object, Euclidean2DPosition> environment = new Continuous2DEnvironment<>(INCARNATION);
        final Node<Object> node = INCARNATION.createNode(rng, environment, null);
        assertNotNull(node);
        final TimeDistribution<Object> immediately = INCARNATION.createTimeDistribution(rng, environment, node, null);
        assertNotNull(immediately);
        assertTrue(Double.isInfinite(immediately.getRate()));
        assertTrue(immediately.getRate() > 0);
        final TimeDistribution<Object> standard = INCARNATION.createTimeDistribution(rng, environment, node, "3");
        assertNotNull(standard);
        assertEquals(3d, standard.getRate(), Double.MIN_VALUE);
        final Reaction<Object> generic = INCARNATION.createReaction(rng, environment, node, standard, null);
        assertNotNull(generic);
        assertTrue(generic instanceof Event);
        final Reaction<Object> program = INCARNATION.createReaction(rng, environment, node, standard, "nbr(1)");
        testIsProtelisProgram(program);
        final Reaction<Object> program2 = INCARNATION.createReaction(rng, environment, node, standard, "testprotelis:test");
        testIsProtelisProgram(program2);
        try {
            INCARNATION.createReaction(rng, environment, node, standard, "send");
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
        node.addReaction(program);
        node.addReaction(program2);
        try {
            INCARNATION.createReaction(rng, environment, node, standard, "send");
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
        node.removeReaction(program2);
        final Reaction<Object> send = INCARNATION.createReaction(rng, environment, node, standard, "send");
        testIsSendToNeighbor(send);
    }

    private static void testIsProtelisProgram(final Reaction<Object> program) {
        assertNotNull(program);
        assertTrue(program instanceof Event);
        assertTrue(program.getConditions().isEmpty());
        assertFalse(program.getActions().isEmpty());
        assertEquals(1, program.getActions().size());
        final Action<Object> prog = program.getActions().get(0);
        assertNotNull(prog);
        assertTrue(prog instanceof RunProtelisProgram);
    }

    private static void testIsSendToNeighbor(final Reaction<Object> program) {
        assertNotNull(program);
        assertTrue(program instanceof ChemicalReaction);
        assertFalse(program.getConditions().isEmpty());
        assertEquals(1, program.getConditions().size());
        final Condition<Object> check = program.getConditions().get(0);
        assertNotNull(check);
        assertTrue(check instanceof ComputationalRoundComplete);
        assertFalse(program.getActions().isEmpty());
        assertEquals(1, program.getActions().size());
        final Action<Object> prog = program.getActions().get(0);
        assertNotNull(prog);
        assertTrue(prog instanceof SendToNeighbor);
    }

    /**
     * Verifies that the incarnation can properly init new concentrations.
     */
    @Test
    void testCreateConcentration() {
        assertEquals("aString", INCARNATION.createConcentration("aString"));
        assertEquals(1.0, INCARNATION.createConcentration("1"));
        assertEquals("foo", INCARNATION.createConcentration("let a = \"foo\"; a"));
    }

}
