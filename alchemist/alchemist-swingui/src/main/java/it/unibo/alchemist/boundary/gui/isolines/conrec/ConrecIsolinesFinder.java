/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.isolines.conrec;

import it.unibo.alchemist.boundary.gui.isolines.IsolinesFinder;
import it.unibo.alchemist.boundary.gui.isolines.Isoline;
import it.unibo.alchemist.boundary.gui.isolines.IsolinesFactory;
import it.unibo.alchemist.boundary.gui.isolines.Segment2D;

import java.util.Collection;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Conrec algorithm adapter to IsolinesFinder interface.
 */
public class ConrecIsolinesFinder implements IsolinesFinder {

    private static final int SAMPLES = 100; // for each dimension

    private final IsolinesFactory factory;

    /**
     * @param factory - the factory used to create segments and isolines
     */
    public ConrecIsolinesFinder(final IsolinesFactory factory) {
        this.factory = factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Isoline> findIsolines(final BidimensionalFunction f, final Number x1, final Number y1, final Number x2, final Number y2, final Collection<Number> levels) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x1);
        Objects.requireNonNull(y1);
        Objects.requireNonNull(x2);
        Objects.requireNonNull(y2);
        Objects.requireNonNull(levels);
        // preparing parameters for the algorithm
        final double startX = x1.doubleValue();
        final double startY = y1.doubleValue();
        final double endX = x2.doubleValue();
        final double endY = y2.doubleValue();
        // the rectangular region defined by start and end is continuous,
        // this means we will sample a discrete amount of points in that region.
        // How many points will be sampled on each side is defined by SAMPLE,
        // step is the distance between two of these points
        final double stepX = (endX - startX) / SAMPLES;
        final double stepY = (endY - startY) / SAMPLES;
        // indexes for accessing x, y and d arrays
        final int ilb = 0;
        final int jlb = 0;
        final int iub = SAMPLES;
        final int jub = SAMPLES;
        // x and y contains respectively the x and y coordinates of the sampling points
        final double[] x = new double[iub + 1]; // +1 because iub, jub are included
        final double[] y = new double[jub + 1];
        for (int i = ilb; i <= iub; i++) {
            if (i == ilb) {
                x[i] = startX;
            } else {
                x[i] = x[i - 1] + stepX;
            }
        }
        for (int j = jlb; j <= jub; j++) {
            if (j == jlb) {
                y[j] = startY;
            } else {
                y[j] = y[j - 1] + stepY;
            }
        }
        // d contains the data of the sampling points, d[i][j] = f(x[i], x[j])
        final double[][] d = new double[iub + 1][jub + 1];
        for (int i = ilb; i <= iub; i++) {
            for (int j = jlb; j <= jub; j++) {
                d[i][j] = f.apply(x[i], y[j]).doubleValue();
            }
        }
        // finding the isolines
        final Map<Double, List<Segment2D>> isolines = new HashMap<>();
        new Conrec((startX1, startY1, endX1, endY1, contourLevel) -> { // render
            final Segment2D segment = factory.makeSegment(startX1, startY1, endX1, endY1);
            final double flooredValue = Math.floor(contourLevel * 10) / 10;
            if (!isolines.containsKey(flooredValue)) {
                isolines.put(flooredValue, new ArrayList<>());
            }
            isolines.get(flooredValue).add(segment);
        }).contour(d, ilb, iub, jlb, jub, x, y, levels.size(), levels.stream().mapToDouble(Number::doubleValue).toArray());
        return isolines.entrySet().stream().map(entry -> factory.makeIsoline(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Isoline> findIsolines(final BidimensionalFunction f, final Segment2D diagonal, final Collection<Number> levels) {
        Objects.requireNonNull(diagonal);
        return this.findIsolines(f, diagonal.getX1(), diagonal.getY1(), diagonal.getX2(), diagonal.getY2(), levels);
    }
}
