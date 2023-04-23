/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model;


import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.nodes.GenericNode;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.implementations.properties.CircularCell;
import it.unibo.alchemist.model.implementations.reactions.BiochemicalReactionBuilder;
import it.unibo.alchemist.model.timedistributions.ExponentialTime;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Factory for the biochemistry incarnation entities.
 */
public final class BiochemistryIncarnation implements Incarnation<Double, Euclidean2DPosition> {

    @Override
    public double getProperty(final Node<Double> node, final Molecule molecule, final String property) {
        return node.getConcentration(molecule);
    }

    @Override
    public Biomolecule createMolecule(final String s) {
        return new Biomolecule(s);
    }

    @Override
    public Node<Double> createNode(
            final RandomGenerator randomGenerator,
            final Environment<Double, Euclidean2DPosition> environment,
            final String parameter
    ) {
        final Node<Double> node = new GenericNode<>(this, environment);
        if (parameter == null || parameter.isEmpty()) {
            node.addProperty(new CircularCell(environment, node));
        } else {
            node.addProperty(new CircularCell(environment, node, Double.parseDouble(parameter)));
        }
        return node;
    }

    @Override
    public TimeDistribution<Double> createTimeDistribution(
            final RandomGenerator randomGenerator,
            final Environment<Double, Euclidean2DPosition> environment,
            final Node<Double> node,
            final String parameter
    ) {
        if (parameter == null || parameter.isEmpty()) {
            return new ExponentialTime<>(1.0, randomGenerator);
        }
        try {
            final double rate = Double.parseDouble(parameter);
            return new ExponentialTime<>(rate, randomGenerator);
        } catch (NumberFormatException e) {
            return new ExponentialTime<>(1.0, randomGenerator);
        }
    }

    @Override
    public Reaction<Double> createReaction(final RandomGenerator randomGenerator,
            final Environment<Double, Euclidean2DPosition> environment,
            final Node<Double> node,
            final TimeDistribution<Double> timeDistribution,
            final String parameter) {
        return new BiochemicalReactionBuilder<>(this, node, environment)
                .randomGenerator(randomGenerator)
                .timeDistribution(timeDistribution)
                .program(parameter)
                .build();
    }

    @Override
    public Condition<Double> createCondition(
        final RandomGenerator randomGenerator,
        final Environment<Double, Euclidean2DPosition> environment,
        final Node<Double> node,
        final TimeDistribution<Double> time,
        final Actionable<Double> actionable,
        final String additionalParameters
    ) {
        return null;
    }

    @Override
    public Action<Double> createAction(
        final RandomGenerator randomGenerator,
        final Environment<Double, Euclidean2DPosition> environment,
        final Node<Double> node,
        final TimeDistribution<Double> time,
        final Actionable<Double> actionable,
        final String additionalParameters
    ) {
        return null;
    }

    @Override
    public Double createConcentration(final String s) {
        if (s == null) { // default value
            return 1d;
        }
        return Double.parseDouble(s);
    }

    @Override
    public Double createConcentration() {
        return 0d;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
