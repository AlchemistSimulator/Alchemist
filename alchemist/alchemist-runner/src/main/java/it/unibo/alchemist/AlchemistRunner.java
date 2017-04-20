/**
 * 
 */
package it.unibo.alchemist;

import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Loader loader;
    private final Time endTime;
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
         * @param t
         *            end time
         * @return builder
         */
        public Builder setEndTime(final Number t) {
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
            return new AlchemistRunner(this.loader, this.endTime, this.exportFileRoot, this.effectsFile,
                    this.samplingInt, this.parallelism, this.headless, this.closeOperation, this.benchmark);
        }
    }

    private AlchemistRunner(final Loader source, final Time endTime, final Optional<String> exportRoot,
            final Optional<String> effectsFile, final double sampling, final int parallelism, final boolean headless,
            final int closeOperation, final boolean benchmark) {
        this.effectsFile = effectsFile;
        this.endTime = endTime;
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
     */
    public void launch(final String... variables) {
        Optional<? extends Throwable> exception = Optional.empty();
        if (variables != null && variables.length > 0) {
            /*
             * Batch mode
             */
            final Map<String, Variable> simVars = getVariables();
            final List<Entry<String, Variable>> varStreams = simVars.entrySet().stream()
                    .filter(e -> ArrayUtils.contains(variables, e.getKey())).collect(Collectors.toList());
            final ExecutorService executor = Executors.newFixedThreadPool(parallelism);
            final Optional<Long> start = Optional.ofNullable(doBenchmark ? System.nanoTime() : null);
            final Stream<Future<Optional<Throwable>>> futureErrors = runWith(Collections.emptyMap(),
                    varStreams, 0, exportFileRoot, loader, samplingInterval, Long.MAX_VALUE, endTime,
                    sim -> {
                        sim.play();
                        sim.run();
                        return sim.getError();
                    })
                    .map(executor::submit);
            final Queue<Future<Optional<Throwable>>> allErrors = futureErrors.collect(Collectors.toCollection(LinkedList::new));
            while (!(exception.isPresent() || allErrors.isEmpty())) {
                try {
                    exception = allErrors.remove().get();
                } catch (InterruptedException | ExecutionException e1) {
                    exception = Optional.of(e1);
                }
            }
//                    futureErrors.parallel()
//                        .map(future -> {
//                        try {
//                            final Optional<Throwable> result = future.get();
//                            return result;
//                        } catch (Exception e) {
//                            return Optional.of(e);
//                        }
//                    })
//                    .filter(Optional::isPresent)
//                    .map(Optional::get)
//                    .findAny();
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
                localEx = runWith(Collections.emptyMap(), null, 0, exportFileRoot, loader, samplingInterval, Long.MAX_VALUE, endTime,
                        sim -> {
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
                        }).findAny().get().call();
            } catch (Exception e) {
                localEx = Optional.of(e);
            }
            exception = localEx;
        }
        if (exception.isPresent()) {
            throw new IllegalStateException(exception.get());
        }
    }

    private static <T, R> Stream<Callable<R>> runWith(final Map<String, Double> baseVarMap,
            final List<Entry<String, Variable>> varStreams, final int pos, final Optional<String> filebase,
            final Loader loader, final double sample, final long endStep, final Time endTime,
            final Function<Simulation<T>, R> afterCreation) {
        if (varStreams == null || pos == varStreams.size()) {
            return Stream.of(() -> {
                final Environment<T> env = loader.getWith(baseVarMap);
                final Simulation<T> sim = new Engine<>(env, endStep, endTime);
                if (filebase.isPresent()) {
                    /*
                     * Make the header: get all the default values and
                     * substitute those that are different in this run
                     */
                    final Map<String, Double> vars = loader.getVariables().entrySet().parallelStream()
                            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDefault()));
                    vars.putAll(baseVarMap);
                    final String header = vars.entrySet().stream().map(e -> e.getKey() + " = " + e.getValue())
                            .collect(Collectors.joining(", "));
                    try {
                        final Exporter<T> exp = new Exporter<>(filebase.get() + ".txt", sample, header,
                                loader.getDataExtractors());
                        sim.addOutputMonitor(exp);
                    } catch (FileNotFoundException e1) {
                        L.error("Could not create " + filebase, e1);
                    }
                }
                return afterCreation.apply(sim);
            });
        } else {
            final Entry<String, Variable> pair = varStreams.get(pos);
            final String varName = pair.getKey();
            return pair.getValue().stream().boxed().parallel().flatMap(v -> {
                final Map<String, Double> newBase = new LinkedHashMap<>(baseVarMap);
                newBase.put(varName, v);
                final Optional<String> newFile = filebase.map(base -> base + "_" + varName + "-" + v);
                return runWith(newBase, varStreams, pos + 1, newFile, loader, sample, endStep, endTime, afterCreation);
            });
        }
    }

}
