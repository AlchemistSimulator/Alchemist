/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.implementations;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Time;

//TODO improve doc

/**
 * This class is a builder for Engine. The default values for an instance of
 * {@link Engine} are:
 * <ul>
 * <li>time = Infinity</li>
 * <li>maxSteps = Long.MAX_VALUE</li>
 * <li>batchSize = 1 (set to > 1 for a batched multithreaded engine)</li>
 * <li>outputReplayStrategy = Aggregate (only relevant to a batched multithreaded engine)</li>
 * </ul>
 */
public class EngineBuilder<T, P extends Position<? extends P>> {

    private final Environment<T, P> environment;
    private long maxSteps = Long.MAX_VALUE;
    private Time time = Time.INFINITY;
    private int batchSize = 1;
    private BatchEngine.OutputReplayStrategy outputReplayStrategy = BatchEngine.OutputReplayStrategy.AGGREGATE;

    public EngineBuilder(final Environment<T, P> environment) {
        this.environment = environment;
    }

    public EngineBuilder<T, P> withMaxSteps(final int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps cannot be <= 0");
        }
        this.maxSteps = maxSteps;
        return this;
    }

    public EngineBuilder<T, P> withTime(final Time time) {
        if (time == null) {
            throw new IllegalArgumentException("time cannot be null");
        }
        this.time = time;
        return this;
    }

    public EngineBuilder<T, P> withBatchSize(final int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize cannot be <= 0");
        }
        this.batchSize = batchSize;
        return this;
    }

    public EngineBuilder<T, P> withOutputReplayStrategy(final BatchEngine.OutputReplayStrategy outputReplayStrategy) {
        this.outputReplayStrategy = outputReplayStrategy;
        return this;
    }

    public Engine<T, P> build() {
        if (this.batchSize != 1) {
            return new BatchEngine<>(environment, maxSteps, time, batchSize, outputReplayStrategy);
        } else {
            return new Engine<>(environment, maxSteps, time);
        }
    }

    /**
     * Constructs a new EngineBuilder
     *
     * @param <T> concentration type
     * @param <P> {@link Position} type
     * @return an instance of {@link EngineBuilder} used
     */
    public static <T, P extends Position<? extends P>> EngineBuilder<T, P> newInstance(Environment<T, P> environment) {
        return new EngineBuilder<>(environment);
    }

}
