/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.export;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import it.unibo.alchemist.model.interfaces.BenchmarkableEnvironment;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Exports the stats about the performance of the environment.
 *
 */
public final class EnvPerformanceStats implements Extractor {

    private static final List<String> COLNAME;
    private static final double[] EMPTY = new double[0];
    static {
        final List<String> cName = new LinkedList<>();
        cName.add("envPerformance");
        COLNAME = Collections.unmodifiableList(cName);
    }

    @Override
    public double[] extractData(final Environment<?, ?> env, final Reaction<?> r, final Time time, final long step) {
        if (env instanceof BenchmarkableEnvironment) {
            return new double[]{((BenchmarkableEnvironment<?, ?>) env).getBenchmarkResult()};
        }
        return EMPTY;
    }

    @Override
    public List<String> getNames() {
        return COLNAME;
    }

}
