/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import it.unibo.alchemist.model.ProtelisIncarnation;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.actions.SendToNeighbor;
import it.unibo.alchemist.model.implementations.conditions.ComputationalRoundComplete;
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.implementations.reactions.ChemicalReaction;
import it.unibo.alchemist.model.implementations.reactions.Event;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;


/**
 */
public class TestIncarnation {

    private static final ProtelisIncarnation INC = ProtelisIncarnation.instance();

    /**
     * Tests the ability of {@link ProtelisIncarnation} of properly building a
     * Alchemist entities for running Protelis.
     */
    @Test
    public void testBuild() {
        final RandomGenerator rng = new MersenneTwister(0);
        final Environment<Object> env = new Continuous2DEnvironment<>();
        final Node<Object> node = INC.createNode(rng, env, null);
        assertNotNull(node);
        final TimeDistribution<Object> immediately = INC.createTimeDistribution(rng, env, node, null);
        assertNotNull(immediately);
        assertTrue(Double.isInfinite(immediately.getRate()));
        assertTrue(immediately.getRate() > 0);
        final TimeDistribution<Object> standard = INC.createTimeDistribution(rng, env, node, "3");
        assertNotNull(standard);
        assertEquals(3d, standard.getRate(), 0d);
        final Reaction<Object> generic = INC.createReaction(rng, env, node, standard, null);
        assertNotNull(generic);
        assertTrue(generic instanceof Event);
        final Reaction<Object> program = INC.createReaction(rng, env, node, standard, "nbr(1)");
        testIsProtelisProgram(program);
        final Reaction<Object> program2 = INC.createReaction(rng, env, node, standard, "testprotelis:test");
        testIsProtelisProgram(program2);
        try {
            INC.createReaction(rng, env, node, standard, "send");
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
        node.addReaction(program);
        node.addReaction(program2);
        try {
            INC.createReaction(rng, env, node, standard, "send");
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
        node.removeReaction(program2);
        final Reaction<Object> send = INC.createReaction(rng, env, node, standard, "send");
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
    public void testCreateConcentration() {
        assertEquals("test", INC.createConcentration("test"));
        assertEquals(1.0, INC.createConcentration("1"));
        assertEquals("foo", INC.createConcentration("let a = \"foo\"; a"));
    }

}
