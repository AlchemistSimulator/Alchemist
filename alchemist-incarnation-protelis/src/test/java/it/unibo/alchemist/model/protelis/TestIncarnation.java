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
import it.unibo.alchemist.model.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.times.DoubleTime;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import org.protelis.lang.datatype.Option;

import javax.annotation.Nonnull;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestIncarnation {

    private static final ProtelisIncarnation<Euclidean2DPosition> INCARNATION = new ProtelisIncarnation<>();
    private static final String PROGRAM = "nbr(1)";
    private static final String SEND = "send";
    private static final double TOLERANCE = 1e-12;
    private static final String UNIT_RATE = "1";

    @Test
    void disposingOneSendMustNotDisposeAnotherSendConditionSharingTheProgram() {
        final RandomGenerator rng = new MersenneTwister(0);
        final Environment<Object, Euclidean2DPosition> environment = new Continuous2DEnvironment<>(INCARNATION);
        final Node<Object> node = INCARNATION.createNode(rng, environment, null);
        environment.addNode(node, environment.makePosition(0, 0));
        final NodeReaction<Object> program = INCARNATION.createNodeReaction(
            rng,
            environment,
            node,
            INCARNATION.createTimeDistribution(rng, environment, node, UNIT_RATE),
            PROGRAM
        );
        node.addReaction(program);
        final RunProtelisProgram<?> programAction = assertInstanceOf(
            RunProtelisProgram.class,
            program.getActions().get(0)
        );
        final CountingDistribution survivingDistribution = new CountingDistribution();
        final GenericReaction<Object> removedSend = new GenericReaction<>(node, new CountingDistribution());
        final GenericReaction<Object> survivingSend = new GenericReaction<>(node, survivingDistribution);
        removedSend.setConditions(List.of(new ComputationalRoundComplete(removedSend, programAction)));
        survivingSend.setConditions(List.of(new ComputationalRoundComplete(survivingSend, programAction)));
        removedSend.initializationComplete(Time.ZERO, environment);
        survivingSend.initializationComplete(Time.ZERO, environment);
        removedSend.dispose();
        program.execute();
        assertTrue(survivingSend.getCanExecute().getCurrent());
        assertEquals(1, survivingDistribution.samples);
        assertEquals(1.0, survivingSend.getNextOccurrence().getCurrent().toDouble(), TOLERANCE);
    }

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
        final NodeReaction<Object> immediateReaction = INCARNATION.createNodeReaction(rng, environment, node, immediately, null);
        final TimeDistributedReaction<?> immediateRecurrence =
            assertInstanceOf(TimeDistributedReaction.class, immediateReaction);
        assertEquals(0d, immediateRecurrence.getMeanDelay());
        final TimeDistribution<Object> standard = INCARNATION.createTimeDistribution(rng, environment, node, "3");
        assertNotNull(standard);
        final NodeReaction<Object> generic = INCARNATION.createNodeReaction(rng, environment, node, standard, null);
        assertEquals(1d / 3, assertInstanceOf(TimeDistributedReaction.class, generic).getMeanDelay());
        assertNotNull(generic);
        assertInstanceOf(GenericReaction.class, generic);
        final NodeReaction<Object> program = INCARNATION.createNodeReaction(rng, environment, node, standard, PROGRAM);
        testIsProtelisProgram(program);
        final NodeReaction<Object> program2 =
            INCARNATION.createNodeReaction(rng, environment, node, standard, "testprotelis:test");
        testIsProtelisProgram(program2);
        try {
            INCARNATION.createNodeReaction(rng, environment, node, standard, SEND);
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
        node.addReaction(program);
        node.addReaction(program2);
        try {
            INCARNATION.createNodeReaction(rng, environment, node, standard, SEND);
            fail();
        } catch (final IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
        node.removeReaction(program2);
        final NodeReaction<Object> send = INCARNATION.createNodeReaction(rng, environment, node, standard, SEND);
        testIsSendToNeighbor(send);
    }

    /**
     * Verifies that the incarnation can properly init new concentrations.
     */
    @Test
    void testCreateConcentration() {
        assertEquals("aString", INCARNATION.createConcentration("aString"));
        assertEquals(1.0, INCARNATION.createConcentration(UNIT_RATE));
        assertEquals("foo", INCARNATION.createConcentration("let a = \"foo\"; a"));
        final Object missing = INCARNATION.createConcentration(null);
        assertFalse(assertInstanceOf(Option.class, missing).isPresent());
    }

    @Test
    void testExponentialSendSchedulingConsumesOneSamplePerCompletedRound() {
        final RandomGenerator rng = new MersenneTwister(0);
        final RandomGenerator dedicatedRng = mock(RandomGenerator.class);
        when(dedicatedRng.nextDouble()).thenReturn(0.5);
        final Environment<Object, Euclidean2DPosition> environment = new Continuous2DEnvironment<>(INCARNATION);
        final Node<Object> node = INCARNATION.createNode(rng, environment, null);
        environment.addNode(node, environment.makePosition(0, 0));
        final TimeDistribution<Object> programDistribution = INCARNATION.createTimeDistribution(
            rng, environment, node, UNIT_RATE
        );
        final NodeReaction<Object> program = INCARNATION.createNodeReaction(
            rng, environment, node, programDistribution, PROGRAM
        );
        node.addReaction(program);
        final TimeDistribution<Object> distribution = new ExponentialTime<>(1.0, dedicatedRng);
        final NodeReaction<Object> reaction = INCARNATION.createNodeReaction(rng, environment, node, distribution, SEND);
        node.addReaction(reaction);
        program.initializationComplete(Time.ZERO, environment);
        reaction.initializationComplete(Time.ZERO, environment);
        verify(dedicatedRng, times(0)).nextDouble();
        assertEquals(Time.INFINITY, reaction.getNextOccurrence().getCurrent());
        assertFalse(reaction.getCanExecute().getCurrent());
        program.execute();
        verify(dedicatedRng, times(1)).nextDouble();
        assertTrue(reaction.getCanExecute().getCurrent());
        assertTrue(reaction.getNextOccurrence().getCurrent().isFinite());
        program.execute();
        verify(dedicatedRng, times(1)).nextDouble();
        assertTrue(reaction.getCanExecute().getCurrent());
        reaction.execute();
        verify(dedicatedRng, times(1)).nextDouble();
        assertFalse(reaction.getCanExecute().getCurrent());
        assertEquals(Time.INFINITY, reaction.getNextOccurrence().getCurrent());
        program.execute();
        verify(dedicatedRng, times(2)).nextDouble();
        assertTrue(reaction.getCanExecute().getCurrent());
        assertTrue(reaction.getNextOccurrence().getCurrent().isFinite());
    }

    @Test
    void testSendSchedulingStartsOnlyWhenTheProgramCompletes() {
        final RandomGenerator rng = new MersenneTwister(0);
        final Environment<Object, Euclidean2DPosition> environment = new Continuous2DEnvironment<>(INCARNATION);
        final Node<Object> node = INCARNATION.createNode(rng, environment, null);
        environment.addNode(node, environment.makePosition(0, 0));
        final TimeDistribution<Object> programDistribution = INCARNATION.createTimeDistribution(
            rng, environment, node, UNIT_RATE
        );
        final NodeReaction<Object> program = INCARNATION.createNodeReaction(
            rng, environment, node, programDistribution, PROGRAM
        );
        node.addReaction(program);
        final CountingDistribution distribution = new CountingDistribution();
        final NodeReaction<Object> reaction = INCARNATION.createNodeReaction(rng, environment, node, distribution, SEND);
        assertInstanceOf(TimeDistributedReaction.class, reaction);
        node.addReaction(reaction);
        program.initializationComplete(Time.ZERO, environment);
        reaction.initializationComplete(Time.ZERO, environment);
        assertEquals(0, distribution.samples);
        assertEquals(Time.INFINITY, reaction.getNextOccurrence().getCurrent());
        assertFalse(reaction.getCanExecute().getCurrent());
        program.execute();
        assertTrue(reaction.getCanExecute().getCurrent());
        assertEquals(1, distribution.samples);
        assertEquals(1.0, reaction.getNextOccurrence().getCurrent().toDouble(), TOLERANCE);
        program.execute();
        assertEquals(1, distribution.samples);
        reaction.execute();
        assertFalse(reaction.getCanExecute().getCurrent());
        assertEquals(Time.INFINITY, reaction.getNextOccurrence().getCurrent());
        assertEquals(1, distribution.samples);
        program.execute();
        assertEquals(2, distribution.samples);
        assertTrue(reaction.getCanExecute().getCurrent());
        assertEquals(3.0, reaction.getNextOccurrence().getCurrent().toDouble(), TOLERANCE);
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

    private static final class CountingDistribution implements TimeDistribution<Object> {
        private int samples;

        @Override
        public double getMeanDelay() {
            // The delays grow at every sample: they follow no single distribution.
            return Double.NaN;
        }

        @Nonnull
        @Override
        public TimeDistribution<Object> newInstanceOn(@Nonnull final Node<Object> node) {
            return new CountingDistribution();
        }

        @Nonnull
        @Override
        public Time sample() {
            samples++;
            return new DoubleTime(samples);
        }
    }

}
