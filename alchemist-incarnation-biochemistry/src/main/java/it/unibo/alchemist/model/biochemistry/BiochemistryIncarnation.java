/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry;


import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule;
import it.unibo.alchemist.model.nodes.GenericNode;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.biochemistry.properties.CircularCell;
import it.unibo.alchemist.model.biochemistry.reactions.BiochemicalReactionBuilder;
import it.unibo.alchemist.model.timedistributions.ExponentialTime;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nullable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

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
            final @Nullable Object parameter
    ) {
        final double diameter = parameter == null ? 0
            : parameter instanceof String ? Double.parseDouble((String) parameter)
            : parameter instanceof Number ? ((Number) parameter).doubleValue()
            : Double.NaN;
        if (Double.isNaN(diameter)) {
            throw new IllegalArgumentException("Invalid diameter: " + parameter);
        }
        final Node<Double> node = new GenericNode<>(this, environment);
        if (diameter == 0) {
            node.addProperty(new CircularCell(environment, node));
        } else {
            node.addProperty(new CircularCell(environment, node, diameter));
        }
        return node;
    }

    @Override
    public TimeDistribution<Double> createTimeDistribution(
        final RandomGenerator randomGenerator,
        final Environment<Double, Euclidean2DPosition> environment,
        final Node<Double> node,
        final @Nullable Object parameter
    ) {
        final var parameterString = Objects.toString(parameter, "");
        final double rate = parameter == null || parameterString.isEmpty() ? 1.0
            : parameter instanceof Number ? ((Number) parameter).doubleValue()
            : Double.parseDouble(parameterString);
        if (Double.isNaN(rate)) {
            throw new IllegalArgumentException("Invalid rate: " + parameter);
        }
        return new ExponentialTime<>(rate, randomGenerator);
    }

    @Override
    public Reaction<Double> createReaction(
        final RandomGenerator randomGenerator,
        final Environment<Double, Euclidean2DPosition> environment,
        final Node<Double> node,
        final TimeDistribution<Double> timeDistribution,
        final @Nullable Object parameter
        ) {
        return new BiochemicalReactionBuilder<>(this, node, environment)
            .randomGenerator(randomGenerator)
            .timeDistribution(timeDistribution)
            .program(
                requireNonNull(parameter, "Biochemical reactions require String a parameter to get built")
                    .toString()
            )
            .build();
    }

    @Override
    public Condition<Double> createCondition(
        final RandomGenerator randomGenerator,
        final Environment<Double, Euclidean2DPosition> environment,
        final Node<Double> node,
        final TimeDistribution<Double> time,
        final Actionable<Double> actionable,
        final @Nullable Object additionalParameters
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
        final @Nullable Object additionalParameters
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
