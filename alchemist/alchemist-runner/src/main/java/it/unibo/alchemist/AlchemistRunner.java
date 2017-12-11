package it.unibo.alchemist;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.view.SingleRunAppBuilder;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.export.EnvPerformanceStats;
import it.unibo.alchemist.loader.export.Exporter;
import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.BenchmarkableEnvironment;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;
import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts Alchemist.
 *
 * @param <T> the concentration type
 */
public final class AlchemistRunner<T> {

    private static final Logger L = LoggerFactory.getLogger(AlchemistRunner.class);
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("alchemist-batch-%d")
            .build();
    private final boolean doBenchmark;
    private final Optional<String> effectsFile;
    private final long endStep;
    private final Time endTime;
    private final Optional<String> exportFileRoot;
    private final boolean headless;
    private final Loader loader;
    private final ImmutableCollection<Supplier<OutputMonitor<T>>> outputMonitors;
    private final int parallelism;
    private final double samplingInterval;

    /**
     * Default private constructor.
     *
     * @param source         the entity that produces the {@link Environment}
     * @param endTime        the time to reach; it could be {@link DoubleTime#INFINITE_TIME infinite}
     * @param endStep        the step to reach; it could be {@link Long#MAX_VALUE infinite}
     * @param exportRoot     the file name to export to
     * @param effectsFile    the file to load {@link EffectFX effects} from
     * @param sampling       the sampling interval
     * @param parallelism    the number of threads in the pool of the {@link Executor}
     * @param headless       if the simulation should run headless or with a GUI
     * @param benchmark      if you want to benchmark this run
     * @param outputMonitors the {@link Collection} of {@link OutputMonitor} to add to the simulation
     * @see AlchemistRunner.Builder
     */
    private AlchemistRunner(final Loader source, final Time endTime, final long endStep,
                            final Optional<String> exportRoot, final Optional<String> effectsFile, final double sampling,
                            final int parallelism, final boolean headless, final boolean benchmark,
                            final ImmutableCollection<Supplier<OutputMonitor<T>>> outputMonitors) {
        this.effectsFile = effectsFile;
        this.endTime = endTime;
        this.endStep = endStep;
        this.exportFileRoot = exportRoot;
        this.headless = headless;
        this.loader = source;
        this.parallelism = parallelism;
        this.samplingInterval = sampling;
        this.doBenchmark = benchmark;
        this.outputMonitors = outputMonitors;
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
        Optional<? extends Throwable> exception = Optional.empty();
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
            final ExecutorService executor = Executors.newFixedThreadPool(parallelism, THREAD_FACTORY);
            final Optional<Long> start = Optional.ofNullable(doBenchmark ? System.nanoTime() : null);
            final Stream<Future<Optional<Throwable>>> futureErrors = prepareSimulations(sim -> {
                if (sim.getEnvironment() instanceof BenchmarkableEnvironment) {
                    for (final Extractor e : loader.getDataExtractors()) {
                        if (e instanceof EnvPerformanceStats) {
                            ((BenchmarkableEnvironment<?>) (sim.getEnvironment())).enableBenchmark();
                        }
                    }
                }
                sim.play();
                sim.run();
                return sim.getError();
            }, variables)
                    .map(executor::submit);
            final Queue<Future<Optional<Throwable>>> allErrors = futureErrors.collect(Collectors.toCollection(LinkedList::new));
            while (!(exception.isPresent() || allErrors.isEmpty())) {
                try {
                    exception = allErrors.remove().get();
                } catch (final InterruptedException | ExecutionException e1) {
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
            start.ifPresent(s -> System.out.printf("Total simulation running time (nanos): %d \n", (System.nanoTime() - s))); //NOPMD: I want to show the result in any case
            executor.shutdown();
            if (exception.isPresent()) {
                executor.shutdownNow();
            }
            try {
                executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
            } catch (final InterruptedException e1) {
                throw new IllegalStateException("The batch execution got interrupted.");
            }
        } else {
            Optional<Throwable> localEx = Optional.empty();
            try {
                localEx = prepareSimulations(sim -> {
                    final boolean onHeadlessEnvironment = GraphicsEnvironment.isHeadless();
                    if (!headless && onHeadlessEnvironment) {
                        L.error("Could not initialize the UI (the graphics environment is headless). Falling back to headless mode.");
                    }
                    if (headless || onHeadlessEnvironment) {
                        sim.play();
                    } else {
                        final SingleRunAppBuilder builder = new SingleRunAppBuilder<>(sim);
                        effectsFile.ifPresent(builder::addEffectGroup);
                        builder.useDefaultEffects(true);
                        builder.build();
                    }
                    sim.run();
                    return sim.getError();
                }, variables).findAny().get().call();
            } catch (final Exception e) {
                localEx = Optional.of(e);
            }
            exception = localEx;
        }
        if (exception.isPresent()) {
            throw new IllegalStateException(exception.get());
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
    private <R> Stream<Callable<R>> prepareSimulations(final Function<Simulation<T>, R> finalizer, final String... variables) {
        final List<List<? extends Entry<String, ?>>> varStreams = Arrays.stream(variables)
                .map(it -> getVariables().get(it).stream()
                        .map(val -> new ImmutablePair<>(it, val))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        return (varStreams.isEmpty()
                ? ImmutableList.of(ImmutableList.<Entry<String, Double>>of())
                : Lists.cartesianProduct(varStreams)).stream()
                .map(vars -> ImmutableMap.copyOf(vars))
                .map(vars -> () -> {
                    final Environment<T> env = loader.getWith(vars);
                    final Simulation<T> sim = new Engine<>(env, endStep, endTime);
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
                        final Map<String, Object> defaultVars = loader.getVariables().entrySet().stream()
                                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDefault()));
                        defaultVars.putAll(vars);
                        final String header = vars.entrySet().stream()
                                .map(e -> e.getKey() + " = " + e.getValue())
                                .collect(Collectors.joining(", "));
                        try {
                            final Exporter<T> exp = new Exporter<>(filename, samplingInterval, header, loader.getDataExtractors());
                            sim.addOutputMonitor(exp);
                        } catch (final FileNotFoundException e) {
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
     */
    public static class Builder<T> {
        private final Loader loader;
        private final Collection<Supplier<OutputMonitor<T>>> outputMonitors = new LinkedList<>();
        private boolean benchmark;
        private int closeOperation;
        private Optional<String> effectsFile = Optional.empty();
        private long endStep = Long.MAX_VALUE;
        private Time endTime = DoubleTime.INFINITE_TIME;
        private Optional<String> exportFileRoot = Optional.empty();
        private boolean headless;
        private int parallelism = Runtime.getRuntime().availableProcessors() + 1;
        private double samplingInt = 1;

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
        public Builder<T> addOutputMonitorSupplier(final Supplier<OutputMonitor<T>> provider) {
            outputMonitors.add(provider);
            return this;
        }

        /**
         * The method builds the {@link AlchemistRunner}.
         *
         * @return {@code AlchemistRunner}
         */
        public AlchemistRunner<T> build() {
            return new AlchemistRunner<>(this.loader, this.endTime, this.endStep, this.exportFileRoot, this.effectsFile,
                    this.samplingInt, this.parallelism, this.headless, this.benchmark,
                    ImmutableList.copyOf(outputMonitors));
        }

        /**
         * Sets the benchkmark mode for the simulation.
         *
         * @param benchmark set true if you want to benchmark this run
         * @return this builder
         */
        public Builder<T> setBenchmarkMode(final boolean benchmark) {
            this.benchmark = benchmark;
            return this;
        }

        /**
         * Sets the uri of the file containing all the {@link EffectFX effects} to apply to the simulation on start.
         *
         * @param uri the uri of the effects file
         * @return this builder
         */
        public Builder<T> setEffects(final String uri) {
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
        public Builder<T> setEndStep(final long steps) {
            if (steps < 0) {
                throw new IllegalArgumentException("The number of steps (" + steps + ") must be zero or positive");
            }
            this.endStep = steps;
            return this;
        }

        /**
         * Sets the time to reach as a number.
         *
         * @param time the end time
         * @return this builder
         * @see #setEndTime(Time)
         */
        public Builder<T> setEndTime(final Number time) {
            final double dt = time.doubleValue();
            if (dt < 0) {
                throw new IllegalArgumentException("The end time (" + dt + ") must be zero or positive");
            }
            this.endTime = new DoubleTime(dt);
            return this;
        }

        /**
         * Sets the time to reach.
         * <p>
         * Default is {@link DoubleTime#INFINITE_TIME infinite}.
         *
         * @param time the end time
         * @return this builder
         */
        public Builder<T> setEndTime(final Time time) {
            this.endTime = time;
            return this;
        }

        /**
         * Sets the GUI default close operation.
         *
         * @param closeOp the close operation
         * @return this builder
         */
        public Builder<T> setGUICloseOperation(final int closeOp) {
            if (closeOp < 0 || closeOp > 3) {
                throw new IllegalArgumentException("The value of close operation is not valid.");
            }
            this.closeOperation = closeOp;
            return this;
        }

        /**
         * Sets whether the simulation will run in headless mode or not.
         *
         * @param headless is headless
         * @return this builder
         */
        public Builder<T> setHeadless(final boolean headless) {
            this.headless = headless;
            return this;
        }

        /**
         * Sets the sampling interval.
         *
         * @param deltaTime the time interval
         * @return this builder
         */
        public Builder<T> setInterval(final double deltaTime) {
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
        public Builder<T> setOutputFile(final String uri) {
            this.exportFileRoot = Optional.ofNullable(uri);
            return this;
        }

        /**
         * Sets the number of threads in the pool of an {@link Executor} the simulation will use.
         *
         * @param threads the threads number
         * @return this builder
         */
        public Builder<T> setParallelism(final int threads) {
            if (threads <= 0) {
                throw new IllegalArgumentException("Thread number must be >= 0");
            }
            this.parallelism = threads;
            return this;
        }
    }

}
