/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.export;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;

import com.google.common.collect.Lists;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Exports the Mean Squared Error for the concentration of some molecule, given
 * another molecule that carries the correct result. The correct value is
 * extracted from every node, then the provided {@link UnivariateStatistic} is
 * applied to get a single, global correct value. Then, the actual value is
 * extracted from every node, its value is compared (subtracted) to the computed
 * correct value, it gets squared, and then logged.
 * 
 * @param <T>
 */
public final class MeanSquaredError<T> implements Extractor {

    private final Incarnation<T, ?> incarnation;
    private final String pReference;
    private final Molecule mReference;
    private final String pActual;
    private final Molecule mActual;
    private final List<String> name;
    private final UnivariateStatistic statistic;

    /**
     * @param localCorrectValueMolecule
     *            expected value {@link Molecule}
     * @param localCorrectValueProperty
     *            expected value property name
     * @param statistics
     *            the {@link UnivariateStatistic} to apply
     * @param localValueMolecule
     *            the target {@link Molecule}
     * @param localValueProperty
     *            the target property
     * @param incarnation
     *            the {@link Incarnation} to use
     */
    public MeanSquaredError(
            final Incarnation<T, ?> incarnation,
            final String localCorrectValueMolecule,
            final String localCorrectValueProperty,
            final String statistics,
            final String localValueMolecule,
            final String localValueProperty) {
        final Optional<UnivariateStatistic> statOpt = StatUtil.makeUnivariateStatistic(statistics);
        if (!statOpt.isPresent()) {
            throw new IllegalArgumentException("Could not create univariate statistic " + statistics);
        }
        statistic = statOpt.get();
        this.incarnation = incarnation;
        this.mReference = incarnation.createMolecule(localCorrectValueMolecule);
        this.pReference = localCorrectValueProperty == null ? "" : localCorrectValueProperty;
        this.pActual = localValueProperty == null ? "" : localValueProperty;
        this.mActual = incarnation.createMolecule(localValueMolecule);
        final StringBuilder mse = new StringBuilder("MSE(");
        mse.append(statistics);
        mse.append('(');
        if (!pReference.isEmpty()) {
            mse.append(pReference);
            mse.append('@');
        }
        mse.append(localCorrectValueMolecule);
        mse.append("),");
        if (!pActual.isEmpty()) {
            mse.append(pActual);
            mse.append('@');
        }
        mse.append(localValueMolecule);
        mse.append(')');
        name = Collections.unmodifiableList(Lists.newArrayList(mse.toString()));
    }

    @Override
    public double[] extractData(final Environment<?, ?> env, final Reaction<?> r, final Time time, final long step) {
        @SuppressWarnings("unchecked")
        final Environment<T, ?> environment = (Environment<T, ?>) env;
        final double value = statistic.evaluate(
                environment.getNodes().parallelStream()
                    .mapToDouble(n -> incarnation.getProperty(n, mReference, pReference))
                    .toArray());
        final double mse = environment.getNodes().parallelStream()
                .mapToDouble(n -> incarnation.getProperty(n, mActual, pActual) - value)
                .map(v -> v * v)
                .average()
                .orElseGet(() -> Double.NaN);
        return new double[]{mse};
    }

    @Override
    public List<String> getNames() {
        return name;
    }

}
