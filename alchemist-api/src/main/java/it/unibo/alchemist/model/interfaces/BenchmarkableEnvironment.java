/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

/**
 * An environment which provides a mean to get infos about its performances.
 *
 * @param <T> Concentration type
 * @param <P> {@link Position} type
 */
public interface BenchmarkableEnvironment<T, P extends Position<? extends P>> extends Environment<T, P> {

    /**
     * Call this method to tell this environment that it should record its performances.
     * Please note that some environments might ignore this message if this method is not called before
     * starting using the environment itself.
     * 
     */
    void enableBenchmark();

    /**
     * @return a double which is a index of the performances
     */
    double getBenchmarkResult();
}
