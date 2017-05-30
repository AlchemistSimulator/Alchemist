/**
 * 
 */
package it.unibo.alchemist;

import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import it.unibo.alchemist.boundary.gui.SingleRunGUI;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.export.Exporter;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Starts Alchemist.
 * 
 */
public final class AlchemistRunner {

    private static final Logger L = LoggerFactory.getLogger(AlchemistRunner.class);
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("alchemist-batch-%d")
            .build();
    private final Loader loader;
    private final Time endTime;
    private final long endStep;
    private final Optional<String> exportFileRoot;
    private final Optional<String> effectsFile;
    private final double samplingInterval;
    private final int parallelism;
    private final boolean headless;
    private final int closeOperation;
    private final boolean doBenchmark;

    /**
     * 
     *
     */
    public static class Builder {
        private int parallelism = Runtime.getRuntime().availableProcessors() + 1;
        private int closeOperation;
        private boolean headless;
        private double samplingInt = 1;
        private final Loader loader;
        private Optional<String> exportFileRoot = Optional.empty();
        private Optional<String> effectsFile = Optional.empty();
        private Time endTime = DoubleTime.INFINITE_TIME;
        private long endStep = Long.MAX_VALUE;
        private boolean benchmark;

        /**
         * 
         * @param loader
         *            loader
         */
        public Builder(final Loader loader) {
            this.loader = Objects.requireNonNull(loader, "Loader can't be null.");
        }

        /**
         * 
         * @param uri
         *            effect uri
         * @return builder
         */
        public Builder setEffects(final String uri) {
            this.effectsFile = Optional.ofNullable(uri);
            return this;
        }

        /**
         * 
         * @param deltaTime
         *            time interval
         * @return builder
         */
        public Builder setInterval(final double deltaTime) {
            if (deltaTime > 0) {
                this.samplingInt = deltaTime;
            } else {
                throw new IllegalArgumentException("A sampling interval negative makes no sense.");
            }
            return this;
        }

        /**
         * 
         * @param uri
         *            output uri
         * @return builder
         */
        public Builder setOutputFile(final String uri) {
            this.exportFileRoot = Optional.ofNullable(uri);
            return this;
        }

        /**
         * 
         * @param t
         *            end time
         * @return builder
         */
        public Builder setEndTime(final Time t) {
            this.endTime = t;
            return this;
        }

        /**
         * 
         * @param steps
         *            end step
         * @return builder
         */
        public Builder setEndStep(final long steps) {
            if (steps < 0) {
                throw new IllegalArgumentException("The number of steps (" + steps + ") must be zero or positive");
            }
            this.endStep = steps;
            return this;
        }

        /**
         * 
         * @param t
         *            end time
         * @return builder
         */
        public Builder setEndTime(final Number t) {
            final double dt = t.doubleValue();
            if (dt < 0) {
                throw new IllegalArgumentException("The end time (" + dt + ") must be zero or positive");
            }
            this.endTime = new DoubleTime(t.doubleValue());
            return this;
        }

        /**
         * 
         * @param headless
         *            is headless
         * @return builder
         */
        public Builder setHeadless(final boolean headless) {
            this.headless = headless;
            return this;
        }

        /**
         * 
         * @param threads
         *            threads number
         * @return builder
         */
        public Builder setParallelism(final int threads) {
            if (threads <= 0) {
                throw new IllegalArgumentException("Thread number must be >= 0");
            }
            this.parallelism = threads;
            return this;
        }

        /**
         * 
         * @param closeOp
         *            the close operation
         * @return buider
         */
        public Builder setGUICloseOperation(final int closeOp) {
            if (closeOp < 0 || closeOp > 3) {
                throw new IllegalArgumentException("The value of close operation is not valid.");
            }
            this.closeOperation = closeOp;
            return this;
        }

        /**
         * @param benchmark set true if you want to benchmark this run
         * @return builder
         */
        public Builder setBenchmarkMode(final boolean benchmark) {
            this.benchmark = benchmark;
            return this;
        }

        /**
         * 
         * @return AlchemistRunner
         */
        public AlchemistRunner build() {
            return new AlchemistRunner(this.loader, this.endTime, this.endStep, this.exportFileRoot, this.effectsFile,
                    this.samplingInt, this.parallelism, this.headless, this.closeOperation, this.benchmark);
        }
    }

    private AlchemistRunner(final Loader source,
            final Time endTime,
            final long endStep,
            final Optional<String> exportRoot,
            final Optional<String> effectsFile,
            final double sampling,
            final int parallelism,
            final boolean headless,
            final int closeOperation,
            final boolean benchmark) {
        this.effectsFile = effectsFile;
        this.endTime = endTime;
        this.endStep = endStep;
        this.exportFileRoot = exportRoot;
        this.headless = headless;
        this.loader = source;
        this.parallelism = parallelism;
        this.samplingInterval = sampling;
        this.closeOperation = closeOperation;
        this.doBenchmark = benchmark;
    }

    /**
     * 
     * @return loader variables
     */
    public Map<String, Variable> getVariables() {
        return loader.getVariables();
    }

    /**
     * 
     * @param variables
     *            loader variables
     * @param <T>
     *            used internally for consistency
     */
    public <T> void launch(final String... variables) {
        Optional<? extends Throwable> exception = Optional.empty();
        if (variables != null && variables.length > 0) {
            /*
             * Batch mode
             */
            final Map<String, Variable> simVars = getVariables();
            for (final String s: variables) {
                if (!simVars.containsKey(s)) {
                    throw new IllegalArgumentException("Variable " + s + " is not allowed. Valid variables are: " + simVars.keySet());
                }
            }
            final ExecutorService executor = Executors.newFixedThreadPool(parallelism, THREAD_FACTORY);
            final Optional<Long> start = Optional.ofNullable(doBenchmark ? System.nanoTime() : null);
            final Stream<Future<Optional<Throwable>>> futureErrors = prepareSimulations(sim -> {
                        sim.play();
                        sim.run();
                        return sim.getError();
                    }, variables)
                    .map(executor::submit);
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
            start.ifPresent(s -> System.out.printf("Total simulation running time (nanos): %d \n", (System.nanoTime() - s))); //NOPMD: I want to show the result in any case
            executor.shutdown();
            if (exception.isPresent()) {
                executor.shutdownNow();
            }
            try {
                executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e1) {
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
                                if (effectsFile.isPresent()) {
                                    SingleRunGUI.make(sim, effectsFile.get(), closeOperation);
                                } else {
                                    SingleRunGUI.make(sim, closeOperation);
                                }
                            }
                            sim.run();
                            return sim.getError();
                        }, variables).findAny().get().call();
            } catch (Exception e) {
                localEx = Optional.of(e);
            }
            exception = localEx;
        }
        if (exception.isPresent()) {
            throw new IllegalStateException(exception.get());
        }
    }

    private <T, R> Stream<Callable<R>> prepareSimulations(final Function<Simulation<T>, R> finalizer, final String... variables) {
        final List<List<? extends Entry<String, Double>>> varStreams = Arrays.stream(variables)
                .map(it -> getVariables().get(it).stream()
                        .mapToObj(val -> new ImmutablePair<>(it, val))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        return (varStreams.isEmpty()
                ? ImmutableList.of(ImmutableList.<Entry<String, Double>>of())
                : Lists.cartesianProduct(varStreams)).stream()
            .map(ImmutableMap::copyOf)
            .map(vars -> () -> {
                final Environment<T> env = loader.getWith(vars);
                final Simulation<T> sim = new Engine<>(env, endStep, endTime);
                if (exportFileRoot.isPresent()) {
                    final String filename = exportFileRoot.get() + (vars.isEmpty() ? "" : "_" + vars.entrySet().stream()
                                .map(e -> e.getKey() + '-' + e.getValue())
                                .collect(Collectors.joining("_")))
                            + ".txt";
                    /*
                     * Make the header: get all the default values and
                     * substitute those that are different in this run
                     */
                    final Map<String, Double> defaultVars = loader.getVariables().entrySet().stream()
                            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDefault()));
                    defaultVars.putAll(vars);
                    final String header = vars.entrySet().stream()
                            .map(e -> e.getKey() + " = " + e.getValue())
                            .collect(Collectors.joining(", "));
                    try {
                        final Exporter<T> exp = new Exporter<>(filename, samplingInterval, header, loader.getDataExtractors());
                        sim.addOutputMonitor(exp);
                    } catch (FileNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                }
                return finalizer.apply(sim);
            });
        }

}
