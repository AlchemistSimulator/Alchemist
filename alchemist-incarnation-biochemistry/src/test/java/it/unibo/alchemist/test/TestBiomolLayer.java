/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.boundary.OutputMonitor;
import it.unibo.alchemist.core.Engine;
import it.unibo.alchemist.core.Simulation;
import it.unibo.alchemist.model.BiochemistryIncarnation;
import it.unibo.alchemist.model.implementations.environments.BioRect2DEnvironment;
import it.unibo.alchemist.model.layers.StepLayer;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.timedistributions.DiracComb;
import it.unibo.alchemist.model.times.DoubleTime;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Layer;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance;
import org.apache.commons.math3.random.MersenneTwister;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestBiomolLayer {

    private static final BiochemistryIncarnation INCARNATION = new BiochemistryIncarnation();

    /**
     * Test if cell status is correctly updated in movement.
     */
    @Test
    void testBiomolStepLayer() {
        final Environment<Double, Euclidean2DPosition> environment = new BioRect2DEnvironment(INCARNATION);
        final Biomolecule b = new Biomolecule("B");
        final Layer<Double, Euclidean2DPosition> bLayer = new StepLayer<>(10_000.0, 0d);
        final MersenneTwister rand = new MersenneTwister(0);
        final Node<Double> cellNode = INCARNATION.createNode(rand, environment, null);
        final Molecule a = new Biomolecule("A");
        final Reaction<Double> underTest = INCARNATION.createReaction(
            rand, environment, cellNode,
            INCARNATION.createTimeDistribution(rand, environment, cellNode, "1"),
            "[B in env] --> [A]"
        );
        cellNode.addReaction(underTest);
        cellNode.addReaction(INCARNATION.createReaction(
            rand, environment, cellNode, new DiracComb<>(100d), "[] --> [BrownianMove(10)]"
        ));
        cellNode.setConcentration(a, 0d);
        environment.setLinkingRule(
                new ConnectWithinDistance<>(2)
        );
        environment.addNode(cellNode, new Euclidean2DPosition(0, 0));
        environment.addLayer(b, bLayer);
        final Simulation<Double, Euclidean2DPosition> sim = new Engine<>(environment, 3000);
        sim.play();
        sim.addOutputMonitor(new OutputMonitor<>() {
            private static final long serialVersionUID = 0L;

            @Override
            public void stepDone(
                @NotNull final Environment<Double, Euclidean2DPosition> environment,
                final Actionable<Double> reaction,
                @Nonnull final Time time,
                final long step
            ) {
                final Euclidean2DPosition curPos = environment.getPosition(environment.getNodeByID(0));
                assertEquals(curPos.getX() > 0 && curPos.getY() > 0, underTest.canExecute());
            }

            @Override
            public void initialized(@Nonnull final Environment<Double, Euclidean2DPosition> environment) {
                stepDone(environment, null, DoubleTime.ZERO, 0);
            }
        });
        sim.run();
        sim.getError().ifPresent(CheckedConsumer.unchecked(it -> {
            throw it;
        }));
    }
}
