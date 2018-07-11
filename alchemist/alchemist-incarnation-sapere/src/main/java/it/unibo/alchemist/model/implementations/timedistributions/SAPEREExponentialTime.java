/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.timedistributions;

import it.unibo.alchemist.expressions.implementations.Expression;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.Time;
import org.danilopianini.lang.HashString;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Allows for a Markovian event whose lambda is computed dynamically using a
 * rate equation.
 * 
 */
public class SAPEREExponentialTime extends ExponentialTime<List<ILsaMolecule>> implements SAPERETimeDistribution {

    /**
     * 
     */
    private static final long serialVersionUID = -687039899173488373L;
    private static final String F_PATTERN = "###.######################";
    private static final DecimalFormat FORMAT = new DecimalFormat(F_PATTERN, DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private final IExpression exp;
    private final double staticRate;
    private final boolean numericRate;
    private Map<HashString, ITreeNode<?>> matches;

    /**
     * @param rateEquation
     *            the rate equation
     * @param random
     *            the {@link RandomGenerator}
     */
    public SAPEREExponentialTime(final String rateEquation, final RandomGenerator random) {
        this(rateEquation, new DoubleTime(), random);
    }

    /**
     * @param rateEquation
     *            the rate equation
     * @param start
     *            initial time
     * @param random
     *            the {@link RandomGenerator}
     */
    public SAPEREExponentialTime(final String rateEquation, final Time start, final RandomGenerator random) {
        super(Double.NaN, start, Objects.requireNonNull(random));
        double temp = 0d;
        boolean numeric = true;
        try {
            temp = Double.parseDouble(Objects.requireNonNull(rateEquation));
        } catch (NumberFormatException e) {
            numeric = false;
        }
        numericRate = numeric;
        staticRate = temp;
        if (numericRate) {
            if (Double.isInfinite(staticRate)) {
                exp = new Expression("asap");
            } else {
                exp = new Expression(FORMAT.format(staticRate));
            }
        } else {
            exp = new Expression(rateEquation);
        }
    }

    @Override
    public double getRate() {
        return numericRate ? staticRate : (Double) exp.calculate(matches).getValue(matches);
    }

    @Override
    public boolean isStatic() {
        return numericRate;
    }

    @Override
    public void setMatches(final Map<HashString, ITreeNode<?>> match) {
        if (!numericRate) {
            matches = match;
        }
    }

    @Override
    public IExpression getRateEquation() {
        return exp;
    }

}
