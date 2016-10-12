/**
 * 
 */
package it.unibo.alchemist;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import it.unibo.alchemist.boundary.gui.AlchemistSwingUI;
import it.unibo.alchemist.boundary.gui.SingleRunGUI;
import it.unibo.alchemist.cli.CLIMaker;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.implementations.Engine.StateCommand;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;
import it.unibo.alchemist.loader.export.Exporter;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Starts Alchemist.
 * 
 */
public final class Alchemist {

    private static final Logger L = LoggerFactory.getLogger(Alchemist.class);
    private static final Map<String, Level> LOGLEVELS;
    private static final String HEADLESS = "hl";
    private static final String VARIABLES = "var";
    private static final char BATCH = 'b';
    private static final char EXPORT = 'e';
    private static final char GRAPHICS = 'g';
    private static final char HELP = 'h';
    private static final char INTERVAL = 'i';
    private static final char TIME = 't';
    private static final char YAML = 'y';

    static {
        final Map<String, Level> levels = new LinkedHashMap<>();
        levels.put("v", Level.INFO);
        levels.put("vv", Level.DEBUG);
        levels.put("vvv", Level.ALL);
        levels.put("q", Level.ERROR);
        levels.put("qq", Level.OFF);
        LOGLEVELS = Collections.unmodifiableMap(levels);
    }

    private Alchemist() {
    }

    /**
     * @param args
     *            the argument for the program
     * @param <T>
     *            concentration type
     */
    public static <T> void main(final String[] args) {
        final Options opts = CLIMaker.getOptions();
        L.trace("Options loaded: {}", opts);
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(opts, args);
            setVerbosity(cmd);
            if (cmd.hasOption(HELP)) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar alchemist-redist-{version}.jar", opts);
                System.exit(0);
            }
            /*
             * Logging level selection
             */
            final Time endTime = getOpt(cmd, TIME, t -> new DoubleTime(Double.parseDouble(t)), DoubleTime.INFINITE_TIME);
            L.info("The simulation will end at {}", endTime);
            Optional<Loader> loader = Optional.empty();
            if (cmd.hasOption(YAML)) {
                try (final InputStream is = new FileInputStream(new File(cmd.getOptionValue(YAML)))) {
                    loader = Optional.of(new YamlLoader(is));
                } catch (IOException e) {
                    L.error("Unable to load the requested file.", e);
                }
            }
            if (loader.isPresent()) {
                final Loader ld = loader.get();
                final String fileName = getOpt(cmd, EXPORT, Function.identity(), null);
                try {
                    final double interval = getOpt(cmd, INTERVAL, Double::parseDouble, 1d);
                    final String[] varsUnderRun = cmd.getOptionValues(VARIABLES);
                    if (cmd.hasOption(BATCH)) {
                        if (varsUnderRun == null) {
                            L.error("You must specify which variables you want the batch to run on.");
                            System.exit(1);
                        }
                        final String[] vars = Optional.ofNullable(cmd.getOptionValues(VARIABLES)).orElse(new String[0]);
                        if (vars.length == 0) {
                            L.info("Alchemist is in batch mode, but no variable is available.");
                            System.exit(2);
                        }
                        final Map<String, Variable> simVars = ld.getVariables();
                        final List<Entry<String, Variable>> varStreams = simVars.entrySet().stream()
                            .filter(e -> ArrayUtils.contains(varsUnderRun, e.getKey()))
                            .collect(Collectors.toList());
                        L.info("Variables: {}", varStreams);
                        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
                        runWith(Collections.emptyMap(), varStreams, 0, fileName, ld, interval, Long.MAX_VALUE, endTime,
                                sim -> {
                                     sim.addCommand(new StateCommand<>().run().build());
                                     sim.run();
                                })
                            .parallel()
                            .forEach(executor::submit);
                        executor.shutdown();
                        try {
                            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
                        } catch (InterruptedException e1) {
                            L.error("Main thread got interrupted.", e1);
                        }
                        System.exit(0);
                    } else {
                        runWith(Collections.emptyMap(), null, 0, fileName, ld, interval, Long.MAX_VALUE, endTime,
                                sim -> {
                                    if (cmd.hasOption(HEADLESS)) {
                                        sim.addCommand(new StateCommand<>().run().build());
                                    } else {
                                        if (GraphicsEnvironment.isHeadless()) {
                                            L.error("Could not initialize the UI (the graphics environment is headless). Falling back to headless mode.");
                                            sim.addCommand(new StateCommand<>().run().build());
                                        } else if (cmd.hasOption(GRAPHICS)) {
                                            SingleRunGUI.make(sim, cmd.getOptionValue(GRAPHICS));
                                        } else {
                                            SingleRunGUI.make(sim);
                                        }
                                    }
                                    sim.run();
                                }).findAny().get().run();
                    }
                } catch (NumberFormatException e) {
                    L.error("A number was expected. " + e.getMessage());
                }
            } else {
                new AlchemistSwingUI().setVisible(true);
            }
        } catch (ParseException e) {
            L.error("Your command sequence could not be parsed.", e);
        }
    }

    private static <R> R getOpt(final CommandLine cmd, final char opt, final Function<String, R> fun, final R def) {
        return cmd.hasOption(opt) ? fun.apply(cmd.getOptionValue(opt)) : def;
    }

    private static <T> Stream<Runnable> runWith(final Map<String, Double> baseVarMap,
            final List<Entry<String, Variable>> varStreams, final int pos,
            final String filebase, final Loader loader,
            final double sample, final long endStep, final Time endTime,
            final Consumer<Simulation<T>> afterCreation) {
        if (varStreams == null || pos == varStreams.size()) {
            return Stream.of(() -> {
                final Environment<T> env = loader.getWith(baseVarMap);
                final Simulation<T> sim = new Engine<>(env, endStep, endTime);
                if (filebase != null) {
                    /*
                     * Make the header: get all the default values and substitute
                     * those that are different in this run
                     */
                    final Map<String, Double> vars = loader.getVariables().entrySet().parallelStream()
                            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDefault()));
                    vars.putAll(baseVarMap);
                    final String header = vars.entrySet().stream()
                            .map(e -> e.getKey() + " = " + e.getValue())
                            .collect(Collectors.joining(", "));
                    try {
                        final Exporter<T> exp = new Exporter<>(filebase + ".txt", sample, header, loader.getDataExtractors());
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
                final String newFile = filebase == null ? null : filebase + "_" + varName + "-" + v;
                return runWith(newBase, varStreams, pos + 1, newFile, loader, sample, endStep, endTime, afterCreation);
            });
        }
    }

    private static void setVerbosity(final CommandLine cmd) {
        boolean oneFound = false;
        for (final Entry<String, Level> entry: LOGLEVELS.entrySet()) {
            if (cmd.hasOption(entry.getKey())) {
                if (oneFound) {
                    /*
                     * If there are conflicting logging level requests, throw errors.
                     */
                    throw new IllegalArgumentException("Conflicting verbosity specification. Only one of " + LOGLEVELS.keySet() + " can be specified.");
                }
                oneFound = true;
                setLogbackLoggingLevel(entry.getValue());
            }
        }
    }

    private static void setLogbackLoggingLevel(final Level level) {
        final ch.qos.logback.classic.Logger root =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

}
