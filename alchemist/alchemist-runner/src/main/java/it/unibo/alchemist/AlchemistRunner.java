/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unibo.alchemist.boundary.gui.view.SingleRunAppBuilder;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.grid.cluster.Cluster;
import it.unibo.alchemist.grid.cluster.ClusterImpl;
import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.LocalGeneralSimulationConfig;
import it.unibo.alchemist.grid.config.SimulationConfig;
import it.unibo.alchemist.grid.config.SimulationConfigImpl;
import it.unibo.alchemist.grid.exceptions.RemoteSimulationException;
import it.unibo.alchemist.grid.simulation.RemoteResult;
import it.unibo.alchemist.grid.simulation.SimulationSet;
import it.unibo.alchemist.grid.simulation.SimulationSetImpl;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.export.EnvPerformanceStats;
import it.unibo.alchemist.loader.export.Exporter;
import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.BenchmarkableEnvironment;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.Time;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.GraphicsEnvironment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Starts Alchemist.
 *
 * @param <T> the concentration type
 * @param <P> the position type
 */
public final class AlchemistRunner<T, P extends Position2D<P>> {

    private static final Logger L = LoggerFactory.getLogger(AlchemistRunner.class);
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("alchemist-batch-%d")
            .build();
    private final Optional<String> effectsFile;
    private final long endStep;
    private final Time endTime;
    private final Optional<String> exportFileRoot;
    private final boolean headless;
    private final Loader loader;
    private final ImmutableCollection<Supplier<OutputMonitor<T, P>>> outputMonitors;
    private final int parallelism;
    private final double samplingInterval;
    private final Optional<String> gridConfigFile;
    private final Optional<String> benchmarkOutputFile;

    /**
     * Default private constructor.
     *
     * @param source                    the entity that produces the {@link Environment}
     * @param endTime                   the time to reach; it could be {@link DoubleTime#INFINITE_TIME infinite}
     * @param endStep                   the step to reach; it could be {@link Long#MAX_VALUE infinite}
     * @param exportRoot                the file name to export to
     * @param effectsFile               the file to load {@link it.unibo.alchemist.boundary.gui.effects.EffectFX effects} from
     * @param sampling                  the sampling interval
     * @param parallelism               the number of threads in the pool of the {@link java.util.concurrent.Executor}
     * @param headless                  if the simulation should run headless or with a GUI
     * @param outputMonitors            the {@link Collection} of {@link OutputMonitor} to add to the simulation
     * @param gridConfigFile            -
     * @param benchmarkOutputFile       -
     * @see AlchemistRunner.Builder
     */
    private AlchemistRunner(final Loader source,
            final Time endTime,
            final long endStep,
            final Optional<String> exportRoot,
            final Optional<String> effectsFile,
            final double sampling,
            final int parallelism,
            final boolean headless,
            final ImmutableCollection<Supplier<OutputMonitor<T, P>>> outputMonitors,
            final Optional<String> gridConfigFile,
            final Optional<String> benchmarkOutputFile) {
        this.effectsFile = effectsFile;
        this.endTime = endTime;
        this.endStep = endStep;
        this.exportFileRoot = exportRoot;
        this.headless = headless;
        this.loader = source;
        this.parallelism = parallelism;
        this.samplingInterval = sampling;
        this.outputMonitors = outputMonitors;
        this.gridConfigFile = gridConfigFile;
        this.benchmarkOutputFile = benchmarkOutputFile;
    }

    /**
     * Getter method for the loader variables.
     *
     * @return loader variables
     */
    public Map<String, Variable<?>> getVariables() {
        return loader.getVariables();
    }

    /**
     * The method launches the simulation.
     *
     * @param variables loader variables
     */
    public void launch(final String... variables) {
        final Optional<? extends Throwable> exception;
        if (variables != null && variables.length > 0) {
            /*
             * Batch mode
             */
            final Map<String, Variable<?>> simVars = getVariables();
            for (final String s : variables) {
                if (!simVars.containsKey(s)) {
                    throw new IllegalArgumentException("Variable " + s + " is not allowed. Valid variables are: " + simVars.keySet());
                }
            }
            if (this.gridConfigFile.isPresent()) {
                exception = launchRemote(variables);
            } else {
                exception = launchLocal(variables);
            }
        } else {
            Optional<Throwable> localEx;
            try {
                localEx = prepareSimulations(sim -> {
                    final boolean onHeadlessEnvironment = GraphicsEnvironment.isHeadless();
                    if (!headless && onHeadlessEnvironment) {
                        L.error("Could not initialize the UI (the graphics environment is headless). Falling back to headless mode.");
                    }
                    if (headless || onHeadlessEnvironment) {
                        sim.play();
                    } else {
                        final SingleRunAppBuilder<T, P> builder = new SingleRunAppBuilder<>(sim);
                        effectsFile.ifPresent(builder::addEffectGroup);
                        builder.useDefaultEffects(true);
                        builder.build();
                    }
                    sim.run();
                    return sim.getError();
                }).findAny().orElse(() -> Optional.of(new IllegalStateException("No simulations are executable"))).call();
            } catch (Exception e) { // NOPMD: desired behavior
                localEx = Optional.of(e);
            }
            exception = localEx;
        }
        if (exception.isPresent()) {
            throw new IllegalStateException(exception.get());
        }
    }

    private Optional<? extends Throwable> launchLocal(final String... variables) {
        Optional<? extends Throwable> exception = Optional.empty();
        /*
         * Local batch mode
         */
        final ExecutorService executor = Executors.newFixedThreadPool(parallelism, THREAD_FACTORY);
        final Optional<Long> start = Optional.ofNullable(benchmarkOutputFile.isPresent() ? System.nanoTime() : null);
        final Stream<Future<Optional<Throwable>>> futureErrors = prepareSimulations(sim -> {
            if (sim.getEnvironment() instanceof BenchmarkableEnvironment) {
                for (final Extractor e : loader.getDataExtractors()) {
                    if (e instanceof EnvPerformanceStats) {
                        ((BenchmarkableEnvironment<?, ?>) (sim.getEnvironment())).enableBenchmark();
                    }
                }
            }
            sim.play();
            sim.run();
            return sim.getError();
        }, variables).map(executor::submit);
        final Queue<Future<Optional<Throwable>>> allErrors = futureErrors.collect(Collectors.toCollection(LinkedList::new));
        while (!(exception.isPresent() || allErrors.isEmpty())) {
            try {
                exception = allErrors.remove().get();
            } catch (InterruptedException | ExecutionException e1) {
                exception = Optional.of(e1);
            }
        }
        /*
         * findAny does NOT short-circuit the stream due to a known bug in
         * the JDK: https://bugs.openjdk.java.net/browse/JDK-8075939
         *
         * Thus, to date, if an exception occurs in a thread which is
         * running a simulation, that exception will be effectively thrown
         * outside that thread only when all the threads have completed
         * their execution. Blame Oracle for this.
         */
        start.ifPresent(e -> printBenchmarkResult(System.nanoTime() - e, false));
        executor.shutdown();
        if (exception.isPresent()) {
            executor.shutdownNow();
        }
        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e1) {
            throw new IllegalStateException("The batch execution got interrupted.", e1);
        }
        return exception;
    }

    private Optional<? extends Throwable> launchRemote(final String... variables) {
        Optional<? extends Throwable> simException = Optional.empty();
        final Optional<Long> start = Optional.ofNullable(benchmarkOutputFile.isPresent() ? System.nanoTime() : null);
        final GeneralSimulationConfig gsc = new LocalGeneralSimulationConfig(this.loader, this.endStep, this.endTime);
        final List<SimulationConfig> simConfigs = getVariablesCartesianProduct(variables).stream()
                .map(SimulationConfigImpl::new)
                .collect(Collectors.toList());
        final SimulationSet set = new SimulationSetImpl(gsc, simConfigs);
        try (Cluster cluster = new ClusterImpl(Paths.get(this.gridConfigFile.orElseThrow(
                () -> new IllegalStateException("No remote configuration file"))))) {
            final Set<RemoteResult> resSet = cluster.getWorkersSet(set.computeComplexity()).distributeSimulations(set);
            for (final RemoteResult res: resSet) {
                this.exportFileRoot.ifPresent(CheckedConsumer.unchecked(res::saveLocally));
            }
            start.ifPresent(e -> printBenchmarkResult(System.nanoTime() - e, true));
        } catch (RemoteSimulationException e) {
            simException = Optional.of(e);
        }
        return simException;
    }

    private List<List<Entry<String, ? extends Serializable>>> getVariablesCartesianProduct(final String... variables) {
        final List<List<? extends Entry<String, ? extends Serializable>>> varStreams = Arrays.stream(variables)
                .map(it -> getVariables().get(it).stream()
                        .map(val -> new ImmutablePair<>(it, val))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        return varStreams.isEmpty() ? ImmutableList.of(ImmutableList.<Entry<String, ? extends Serializable>>of())
                : Lists.cartesianProduct(varStreams);
    }

    private void printBenchmarkResult(final Long value, final boolean distributed) {
        System.out.printf("Total simulation running time (nanos): %d %n", value); // NOPMD: I want to show the result in any case
        if (benchmarkOutputFile.isPresent()) {
            final File f = new File(benchmarkOutputFile.get());
            try {
                FileUtils.forceMkdirParent(f);
                try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriterWithEncoding(f, StandardCharsets.UTF_8, true)))) {
                    final SimpleDateFormat isoTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US);
                    isoTime.setTimeZone(TimeZone.getTimeZone("UTC"));
                    w.println(isoTime.format(new Date())
                            + (distributed ? " - Distributed exc time:" : " - Serial exc time:") + value.toString());
                }
            } catch (final IOException e) {
                L.error(e.getMessage());
            }
        }
    }

    /**
     * The method initializes the simulation.
     *
     * @param finalizer
     * @param variables
     * @param <R>
     * @return
     */
    private <R> Stream<Callable<R>> prepareSimulations(final Function<Simulation<T, P>, R> finalizer, final String... variables) {
        return getVariablesCartesianProduct(variables).stream()
                .map(ImmutableMap::copyOf)
                .map(vars -> () -> {
                    final Environment<T, P> env = loader.getWith(vars);
                    final Simulation<T, P> sim = new Engine<>(env, endStep, endTime);
                    outputMonitors.stream().map(Supplier::get).forEach(sim::addOutputMonitor);
                    if (exportFileRoot.isPresent()) {
                        final String filename = exportFileRoot.get() + (vars.isEmpty() ? "" : "_" + vars.entrySet().stream()
                                .map(e -> e.getKey() + '-' + e.getValue())
                                .collect(Collectors.joining("_")))
                                + ".txt";
                        /*
                         * Make the header: get all the default values and
                         * substitute those that are different in this run
                         */
                        final Map<String, Object> defaultValues = loader.getVariables().entrySet().stream()
                                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDefault()));
                        defaultValues.putAll(vars);
                        final String header = vars.entrySet().stream()
                                .map(e -> e.getKey() + " = " + e.getValue())
                                .collect(Collectors.joining(", "));
                        try {
                            final Exporter<T, P> exp = new Exporter<>(filename, samplingInterval, header, loader.getDataExtractors());
                            sim.addOutputMonitor(exp);
                        } catch (FileNotFoundException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                    return finalizer.apply(sim);
                });
    }

    /**
     * Builder class for {@link AlchemistRunner} instances.
     *
     * @param <T> concentration type
     * @param <P> position type
     */
    public static class Builder<T, P extends Position2D<P>> {
        private final Loader loader;
        private final Collection<Supplier<OutputMonitor<T, P>>> outputMonitors = new LinkedList<>();
        private Optional<String> effectsFile = Optional.empty();
        private long endStep = Long.MAX_VALUE;
        private Time endTime = DoubleTime.INFINITE_TIME;
        private Optional<String> exportFileRoot = Optional.empty();
        private boolean headless;
        private int parallelism = Runtime.getRuntime().availableProcessors() + 1;
        private double samplingInt = 1;
        private Optional<String> gridConfigFile = Optional.empty();
        private Optional<String> benchmarkOutputFile = Optional.empty();

        /**
         * Default constructor for the builder class.
         *
         * @param loader the loader
         */
        public Builder(final Loader loader) {
            this.loader = Objects.requireNonNull(loader, "Loader can't be null.");
        }

        /**
         * Add an {@link OutputMonitor} through a {@code Supplier} function.
         *
         * @param provider the function providing the required {@code OutputMonitor}
         * @return this builder
         */
        public Builder<T, P> addOutputMonitorSupplier(final Supplier<OutputMonitor<T, P>> provider) {
            outputMonitors.add(provider);
            return this;
        }

        /**
         * The method builds the {@link AlchemistRunner}.
         *
         * @return {@code AlchemistRunner}
         */
        public AlchemistRunner<T, P> build() {
            return new AlchemistRunner<>(this.loader, this.endTime, this.endStep, this.exportFileRoot, this.effectsFile,
                    this.samplingInt, this.parallelism, this.headless,
                    ImmutableList.copyOf(outputMonitors), gridConfigFile, benchmarkOutputFile);
        }

        /**
         * Sets the uri of the file containing all the {@link it.unibo.alchemist.boundary.gui.effects.EffectFX effects} to apply to the simulation on start.
         *
         * @param uri the uri of the effects file
         * @return this builder
         */
        public Builder<T, P> withEffects(final String uri) {
            this.effectsFile = Optional.ofNullable(uri);
            return this;
        }

        /**
         * Sets the step to reach.
         * <p>
         * Default is {@link Long#MAX_VALUE infinite}.
         *
         * @param steps the end step
         * @return this builder
         */
        public Builder<T, P> endingAtStep(final long steps) {
            if (steps < 0) {
                throw new IllegalArgumentException("The number of steps (" + steps + ") must be zero or positive");
            }
            this.endStep = steps;
            return this;
        }

        /**

         * Sets the time to reach as a number.
         *
         * @param t the end time
         * @return this builder
         * @see #endingAtTime(Time)
         */
        public Builder<T, P> endingAtTime(final Number t) {
            final double dt = t.doubleValue();
            if (dt < 0) {
                throw new IllegalArgumentException("The end time (" + dt + ") must be zero or positive");
            }
            this.endTime = new DoubleTime(dt);
            return this;
        }

        /**
         *
         * @param t
         *            end time
         * @return builder
         */
        public Builder<T, P> endingAtTime(final Time t) {
            this.endTime = t;
            return this;
        }

        /**
         * Sets whether the simulation will run in headless mode or not.
         *
         * @param headless is headless
         * @return this builder
         */
        public Builder<T, P> headless(final boolean headless) {
            this.headless = headless;
            return this;
        }

        /**
         * Sets the sampling interval.
         *
         * @param deltaTime the time interval
         * @return this builder
         */
        public Builder<T, P> samplingEvery(final double deltaTime) {
            if (deltaTime > 0) {
                this.samplingInt = deltaTime;
            } else {
                throw new IllegalArgumentException("A sampling interval negative makes no sense.");
            }
            return this;
        }

        /**
         * Sets the file where the output will be saved to.
         *
         * @param uri the uri of the output file
         * @return this builder
         */
        public Builder<T, P> writingOutputTo(final String uri) {
            this.exportFileRoot = Optional.ofNullable(uri);
            return this;
        }

        /**
         * Sets the number of threads in the pool of an {@link java.util.concurrent.Executor} the simulation will use.
         *
         * @param threads the threads number
         * @return this builder
         */
        public Builder<T, P> withParallelism(final int threads) {
            if (threads <= 0) {
                throw new IllegalArgumentException("Thread number must be >= 0");
            }
            this.parallelism = threads;
            return this;
        }

        /**
         *
         * @param path Ignite's setting file path
         * @return builder
         */
        public Builder<T, P> withIgniteConfigration(final String path) {
            this.gridConfigFile = Optional.ofNullable(path);
            return this;
        }

        /**
         *
         * @param path Benchmark save file path
         * @return builder
         */
        public Builder<T, P> writingBenchmarkResultsTo(final String path) {
            this.benchmarkOutputFile = Optional.ofNullable(path);
            return this;
        }
    }

}
