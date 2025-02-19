/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.protelis;

import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.protelis.actions.SendToNeighbor;
import it.unibo.alchemist.model.protelis.conditions.ComputationalRoundComplete;
import it.unibo.alchemist.model.reactions.ChemicalReaction;
import it.unibo.alchemist.model.reactions.Event;
import it.unibo.alchemist.protelis.actions.RunProtelisProgram;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 */
class TestIncarnation {

    private static final ProtelisIncarnation<Euclidean2DPosition> INCARNATION = new ProtelisIncarnation<>();
    private static final String SEND = "send";

    /**
     * Tests the ability of {@link ProtelisIncarnation} of properly building an
     * Alchemist entity for running Protelis.
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
        assertInstanceOf(Event.class, generic);
        final Reaction<Object> program = INCARNATION.createReaction(rng, environment, node, standard, "nbr(1)");
        testIsProtelisProgram(program);
        final Reaction<Object> program2 = INCARNATION.createReaction(rng, environment, node, standard, "testprotelis:test");
        testIsProtelisProgram(program2);
        try {
            INCARNATION.createReaction(rng, environment, node, standard, SEND);
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
        node.addReaction(program);
        node.addReaction(program2);
        try {
            INCARNATION.createReaction(rng, environment, node, standard, SEND);
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
        node.removeReaction(program2);
        final Reaction<Object> send = INCARNATION.createReaction(rng, environment, node, standard, SEND);
        testIsSendToNeighbor(send);
    }

    private static void testIsProtelisProgram(final Reaction<Object> program) {
        assertNotNull(program);
        assertInstanceOf(Event.class, program);
        assertTrue(program.getConditions().isEmpty());
        assertFalse(program.getActions().isEmpty());
        assertEquals(1, program.getActions().size());
        final Action<Object> prog = program.getActions().get(0);
        assertNotNull(prog);
        assertInstanceOf(RunProtelisProgram.class, prog);
    }

    private static void testIsSendToNeighbor(final Reaction<Object> program) {
        assertNotNull(program);
        assertInstanceOf(ChemicalReaction.class, program);
        assertFalse(program.getConditions().isEmpty());
        assertEquals(1, program.getConditions().size());
        final Condition<Object> check = program.getConditions().get(0);
        assertNotNull(check);
        assertInstanceOf(ComputationalRoundComplete.class, check);
        assertFalse(program.getActions().isEmpty());
        assertEquals(1, program.getActions().size());
        final Action<Object> prog = program.getActions().get(0);
        assertNotNull(prog);
        assertInstanceOf(SendToNeighbor.class, prog);
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
