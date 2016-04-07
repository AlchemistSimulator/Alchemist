/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors;

import static org.apache.commons.math3.util.FastMath.nextUp;
import static org.apache.commons.math3.util.FastMath.round;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.apache.commons.math3.util.FastMath.nextDown;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Environment2DWithObstacles;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

import java.util.ArrayList;
import java.util.stream.IntStream;

import org.danilopianini.lang.HashUtils;
import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @param <T>
 */
@ExportInspector
public abstract class PositionSampler<T> extends EnvironmentSampler<Position, T> {

    /**
     * 
     */
    public static final int DEFAULT_SAMPLES = 100;
    private static final Logger L = LoggerFactory.getLogger(PositionSampler.class);

    private static final long serialVersionUID = -4687082644114909198L;

    @ExportForGUI(nameToExport = "Samples (positive integer)")
    private String samples = Integer.toString(DEFAULT_SAMPLES);

    private String sCache;
    private Iterable<Position> result;
    private Environment<T> envCache;

    @Override
    protected Iterable<Position> computeSamples(
            final Environment<T> env,
            final Reaction<T> r,
            final Time time,
            final long step) {
        if (!HashUtils.pointerEquals(samples, sCache) || !HashUtils.pointerEquals(env, envCache)) {
            envCache = env;
            try {
                final int n = Integer.parseInt(samples);
                if (n > 0) {
                    sCache = samples;
                    final double sx = env.getSize()[0];
                    final double sy = env.getSize()[1];
                    final double dx = env.getOffset()[0];
                    final double dy = env.getOffset()[1];
                    final int nx = (int) round(sqrt(sx / sy * n));
                    final int ny = (int) round(sqrt(sy / sx * n));
                    final double stepx = sx / nx;
                    final double stepy = sy / ny;
                    final Environment2DWithObstacles<?, ?> oenv = env instanceof Environment2DWithObstacles<?, ?>
                            ? (Environment2DWithObstacles<?, ?>) env
                            : null;
                    result = IntStream.range(0, n).collect(ArrayList::new, (array, i) -> {
                        final double px = dx + stepx * (i % nx);
                        final double py = dy + stepy * ((i / nx) % ny);
                        final Position pos = new Continuous2DEuclidean(px, py);
                        if (oenv == null || !oenv.intersectsObstacle(nextDown(px), nextDown(py), nextUp(px), nextUp(py))) {
                            array.add(pos);
                        }
                    }, (c1, c2) -> c1.addAll(c2));
                } else {
                    L.warn("Update discarded: Samples must be a positive integer value.");
                }
            } catch (NumberFormatException e) {
                L.warn("Update discarded: Samples must be a positive integer value.", e);
            }
        }
        return result;
    }

    /**
     * @return samples
     */
    public String getSamples() {
        return samples;
    }

    /**
     * @param sample
     *            Number of samples
     */
    public void setSamples(final String sample) {
        this.samples = sample;
    }

}
