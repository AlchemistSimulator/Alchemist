/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
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
import it.unibo.alchemist.model.NodeReaction;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.TimeDistributedReaction;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.environments.Continuous2DEnvironment;
import it.unibo.alchemist.model.incarnations.ProtelisIncarnation;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.protelis.actions.RunProtelisProgram;
import it.unibo.alchemist.model.protelis.actions.SendToNeighbor;
import it.unibo.alchemist.model.protelis.conditions.ComputationalRoundComplete;
import it.unibo.alchemist.model.reactions.GenericReaction;
import it.unibo.alchemist.model.times.DoubleTime;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

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
    private static final double TOLERANCE = 1e-12;
    private static final double THIRD_OCCURRENCE = 6.0;

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
        final NodeReaction<Object> immediateReaction = INCARNATION.createReaction(rng, environment, node, immediately, null);
        final TimeDistributedReaction<?> immediateRecurrence =
            assertInstanceOf(TimeDistributedReaction.class, immediateReaction);
        assertTrue(Double.isInfinite(immediateRecurrence.getRate()));
        assertTrue(immediateRecurrence.getRate() > 0);
        final TimeDistribution<Object> standard = INCARNATION.createTimeDistribution(rng, environment, node, "3");
        assertNotNull(standard);
        final NodeReaction<Object> generic = INCARNATION.createReaction(rng, environment, node, standard, null);
        assertEquals(3d, assertInstanceOf(TimeDistributedReaction.class, generic).getRate(), Double.MIN_VALUE);
        assertNotNull(generic);
        assertInstanceOf(GenericReaction.class, generic);
        final NodeReaction<Object> program = INCARNATION.createReaction(rng, environment, node, standard, "nbr(1)");
        testIsProtelisProgram(program);
        final NodeReaction<Object> program2 = INCARNATION.createReaction(rng, environment, node, standard, "testprotelis:test");
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
        final NodeReaction<Object> send = INCARNATION.createReaction(rng, environment, node, standard, SEND);
        testIsSendToNeighbor(send);
    }

    private static void testIsProtelisProgram(final NodeReaction<Object> program) {
        assertNotNull(program);
        assertInstanceOf(GenericReaction.class, program);
        assertTrue(program.getConditions().isEmpty());
        assertFalse(program.getActions().isEmpty());
        assertEquals(1, program.getActions().size());
        final Action<Object> prog = program.getActions().get(0);
        assertNotNull(prog);
        assertInstanceOf(RunProtelisProgram.class, prog);
    }

    private static void testIsSendToNeighbor(final NodeReaction<Object> program) {
        assertNotNull(program);
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

    @Test
    void testSendSchedulingSamplesOnlyWhenAdvancing() {
        final RandomGenerator rng = new MersenneTwister(0);
        final Environment<Object, Euclidean2DPosition> environment = new Continuous2DEnvironment<>(INCARNATION);
        final Node<Object> node = INCARNATION.createNode(rng, environment, null);
        final TimeDistribution<Object> programDistribution = INCARNATION.createTimeDistribution(
            rng, environment, node, "1"
        );
        final NodeReaction<Object> program = INCARNATION.createReaction(
            rng, environment, node, programDistribution, "nbr(1)"
        );
        node.addReaction(program);
        final CountingDistribution distribution = new CountingDistribution();
        final NodeReaction<Object> reaction = INCARNATION.createReaction(rng, environment, node, distribution, SEND);
        final TimeDistributedReaction<?> recurringReaction =
            assertInstanceOf(TimeDistributedReaction.class, reaction);
        node.addReaction(reaction);
        program.initializationComplete(Time.ZERO, environment);
        reaction.initializationComplete(Time.ZERO, environment);
        assertEquals(1, distribution.samples);
        assertEquals(1.0, reaction.getNextOccurrence().getCurrent().toDouble(), TOLERANCE);
        assertFalse(reaction.canExecute().getCurrent());
        recurringReaction.updateSchedulingAfterFiring(new DoubleTime(1.0));
        assertEquals(2, distribution.samples);
        assertEquals(3.0, reaction.getNextOccurrence().getCurrent().toDouble(), TOLERANCE);
        program.execute();
        assertTrue(reaction.canExecute().getCurrent());
        assertEquals(2, distribution.samples);
        assertEquals(3.0, reaction.getNextOccurrence().getCurrent().toDouble(), TOLERANCE);
        recurringReaction.updateSchedulingAfterFiring(new DoubleTime(3.0));
        assertEquals(3, distribution.samples);
        assertEquals(THIRD_OCCURRENCE, reaction.getNextOccurrence().getCurrent().toDouble(), TOLERANCE);
    }

    private static final class CountingDistribution implements TimeDistribution<Object> {
        private int samples;

        @Nonnull
        @Override
        public Time sample() {
            samples++;
            return new DoubleTime(samples);
        }

        @Nonnull
        @Override
        public TimeDistribution<Object> newInstanceOn(@Nonnull final Node<Object> node) {
            return new CountingDistribution();
        }
    }

}
