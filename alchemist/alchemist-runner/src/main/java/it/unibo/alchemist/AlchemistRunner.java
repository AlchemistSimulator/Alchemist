/**
 * 
 */
package it.unibo.alchemist;

import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.gui.SingleRunGUI;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.implementations.Engine.StateCommand;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.export.Exporter;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;
import java8.util.Objects;

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
            if (this.headless) {
                throw new IllegalArgumentException("The simulation is headless.");
            }
            if (closeOp < 0 || closeOp > 3) {
                throw new IllegalArgumentException("The value of close operation is not valid.");
            }
            this.closeOperation = closeOp;
            return this;
        }

        /**
         * 
         * @return AlchemistRunner
         */
        public AlchemistRunner build() {
            return new AlchemistRunner(this.loader, this.endTime, this.exportFileRoot, this.effectsFile,
                    this.samplingInt, this.parallelism, this.headless, this.closeOperation);
        }
    }

    private AlchemistRunner(final Loader source, final Time endTime, final Optional<String> exportRoot,
            final Optional<String> effectsFile, final double sampling, final int parallelism, final boolean headless,
            final int closeOperation) {
        this.effectsFile = effectsFile;
        this.endTime = endTime;
        this.exportFileRoot = exportRoot;
        this.headless = headless;
        this.loader = source;
        this.parallelism = parallelism;
        this.samplingInterval = sampling;
        this.closeOperation = closeOperation;
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
        if (variables != null && variables.length > 0) {
            /*
             * Batch mode
             */
            final Map<String, Variable> simVars = getVariables();
            final List<Entry<String, Variable>> varStreams = simVars.entrySet().stream()
                    .filter(e -> ArrayUtils.contains(variables, e.getKey())).collect(Collectors.toList());
            final ExecutorService executor = Executors.newFixedThreadPool(parallelism);
            runWith(Collections.emptyMap(), varStreams, 0, exportFileRoot, loader, samplingInterval, Long.MAX_VALUE,
                    endTime, sim -> {
                        sim.addCommand(new StateCommand<>().run().build());
                        sim.run();
                    }).parallel().forEach(executor::submit);
            executor.shutdown();
            try {
                executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e1) {
                throw new IllegalStateException("The batch execution got interrupted.");
            }
        } else {
            runWith(Collections.emptyMap(), null, 0, exportFileRoot, loader, samplingInterval, Long.MAX_VALUE, endTime,
                    sim -> {
                        final boolean onHeadlessEnvironment = GraphicsEnvironment.isHeadless();
                        if (!headless && onHeadlessEnvironment) {
                            L.error("Could not initialize the UI (the graphics environment is headless). Falling back to headless mode.");
                        }
                        if (headless || onHeadlessEnvironment) {
                            sim.addCommand(new StateCommand<>().run().build());
                        } else {
                            if (effectsFile.isPresent()) {
                                SingleRunGUI.make(sim, effectsFile.get(), closeOperation);
                            } else {
                                SingleRunGUI.make(sim, closeOperation);
                            }
                        }
                        sim.run();
                    }).findAny().get().run();
        }
    }

    private static <T> Stream<Runnable> runWith(final Map<String, Double> baseVarMap,
            final List<Entry<String, Variable>> varStreams, final int pos, final Optional<String> filebase,
            final Loader loader, final double sample, final long endStep, final Time endTime,
            final Consumer<Simulation<T>> afterCreation) {
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
                afterCreation.accept(sim);
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
