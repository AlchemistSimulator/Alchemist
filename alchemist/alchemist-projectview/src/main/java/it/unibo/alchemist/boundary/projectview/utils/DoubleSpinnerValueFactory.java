/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview.utils;

import java.util.function.DoubleBinaryOperator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.DoubleStringConverter;

/**
 * Class that realizes a DoubleSpinnerValueFactory.
 *
 */
public final class DoubleSpinnerValueFactory extends SpinnerValueFactory<Double> {

    private final double min, max, step, tolerance;

    /**
     * Constructor.
     * 
     * @param min
     *            the min value
     * @param max
     *            the max value
     * @param def
     *            the default vale
     * @param step
     *            the step value
     * @param tolerance
     *            the tolerance value
     */
    public DoubleSpinnerValueFactory(final double min, final double max, final double def, 
            final double step, final double tolerance) {
        if (min > max) {
            throw new IllegalArgumentException("The min value must be smaller than max value.");
        }
        if (def < min || def > max) {
            throw new IllegalArgumentException("The default value must be bigger than min value and smaller than max value.");
        }
        if (step < 0) {
            throw new IllegalArgumentException("The step value must be positive.");
        }
        if (tolerance < 0) {
            throw new IllegalArgumentException("The tolerance value must be positive.");
        }
        setConverter(new DoubleStringConverter());
        this.min = min;
        this.max = max;
        this.step = step;
        this.tolerance = tolerance;
    }

    @Override
    public void decrement(final int steps) {
        computeVal(steps, (a, b) -> a - b);
    }

    @Override
    public void increment(final int steps) {
        computeVal(steps, (a, b) -> a + b);
    }

    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "Exact comparison is desired here")
    private void computeVal(final double steps, final DoubleBinaryOperator op) {
        final double newVal = op.applyAsDouble(getValue(), steps * step);
        final double closestOnScaleMul = Math.round(newVal / step) * step;
        final double closestOnScaleDiv = Math.round(newVal / step) / (1 / step);
        final double closest = closestOnScaleDiv == closestOnScaleMul
                ? closestOnScaleDiv
                : Double.toString(closestOnScaleDiv).length() < Double.toString(closestOnScaleMul).length()
                        ? closestOnScaleDiv : closestOnScaleMul;
        setValue(Math.min(Math.max(Math.abs(newVal - closest) < tolerance ? closest : newVal, min), max));
    }
}
