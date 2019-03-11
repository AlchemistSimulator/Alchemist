/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JFrame;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.ignite.startup.cmdline.CommandLineStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import it.unibo.alchemist.AlchemistRunner.Builder;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import it.unibo.alchemist.cli.CLIMaker;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.YamlLoader;

/**
 * Starts Alchemist.
 * 
 */
public final class Alchemist {

    private static final Logger L = LoggerFactory.getLogger(Alchemist.class);
    private static final Map<String, Level> LOGLEVELS;
    private static final String HEADLESS = "hl";
    private static final String VARIABLES = "var";
    private static final String BENCHMARK = "bmk";
    private static final char BATCH = 'b';
    private static final char EXPORT = 'e';
    private static final char DISTRIBUTED = 'd';
    private static final char GRAPHICS = 'g';
    private static final char HELP = 'h';
    private static final char INTERVAL = 'i';
    private static final char NODE = 's';
    private static final char PARALLELISM = 'p';
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
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(opts, args);
            setVerbosity(cmd);
            if (cmd.hasOption(NODE)) {
                CommandLineStartup.main(new String[] {cmd.getOptionValue(NODE)});
            }
            if (cmd.hasOption(HELP)) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar alchemist-redist-{version}.jar", opts);
                System.exit(0);
            }
            Optional<Loader> loader = Optional.empty();
            if (cmd.hasOption(YAML)) {
                try (InputStream is = new FileInputStream(new File(cmd.getOptionValue(YAML)))) {
                    loader = Optional.of(new YamlLoader(is));
                } catch (IOException e) {
                    L.error("Unable to load the requested file.", e);
                }
            }
            if (loader.isPresent()) {
                final Builder<?, ?> simBuilder = new Builder<>(loader.get())
                        .setHeadless(cmd.hasOption(HEADLESS))
                        .setGUICloseOperation(JFrame.EXIT_ON_CLOSE);
                ifPresent(cmd, EXPORT, simBuilder::setOutputFile);
                ifPresent(cmd, GRAPHICS, simBuilder::setEffects);
                try {
                    ifPresent(cmd, INTERVAL, Double::parseDouble, simBuilder::setInterval);
                    ifPresent(cmd, TIME, Double::parseDouble, simBuilder::setEndTime);
                    final String[] varsUnderRun = cmd.getOptionValues(VARIABLES);
                    if (cmd.hasOption(BATCH)) {
                        if (cmd.hasOption(PARALLELISM)) {
                            try {
                                final int threads = Integer.parseUnsignedInt(cmd.getOptionValue(PARALLELISM));
                                simBuilder.setParallelism(threads);
                                L.info("Using " + threads + " thread(s).");
                            } catch (final NumberFormatException e) {
                                simBuilder.setParallelism(Runtime.getRuntime().availableProcessors());
                                L.warn("Invalid option for PARALLELISM parameter, back to default.");
                            }
                        }
                        if (cmd.hasOption(BENCHMARK)) {
                            simBuilder.setBenchmarkOutputFile(cmd.getOptionValue(BENCHMARK));
                        }
                        if (varsUnderRun == null) {
                            L.error("You must specify which variables you want the batch to run on.");
                            System.exit(1);
                        }
                        final String[] vars = Optional.ofNullable(cmd.getOptionValues(VARIABLES)).orElse(new String[0]);
                        if (vars.length == 0) {
                            L.info("Alchemist is in batch mode, but no variable is available.");
                            System.exit(2);
                        }

                        if (cmd.hasOption(DISTRIBUTED)) {
                            ifPresent(cmd, DISTRIBUTED, simBuilder::setRemoteConfig);
                        }
                        simBuilder.build().launch(vars);
                    } else {
                        simBuilder.build().launch();
                    }
                } catch (NumberFormatException e) {
                    L.error("A number was expected. " + e.getMessage());
                    System.exit(1);
                }
            } else {
                ProjectGUI.main();
            }
        } catch (ParseException e) {
            L.error("Your command sequence could not be parsed.", e);
        }
    }

    private static void ifPresent(final CommandLine cmd, final char opt, final Consumer<String> op) {
        ifPresent(cmd, opt, Function.identity(), op);
    }

    private static <R> void ifPresent(final CommandLine cmd, final char opt, final Function<String, R> fun, final Consumer<R> op) {
        getOpt(cmd, opt, fun).ifPresent(op);
    }

    private static <R> Optional<R> getOpt(final CommandLine cmd, final char opt, final Function<String, R> fun) {
        return Optional.ofNullable(cmd.hasOption(opt) ? fun.apply(cmd.getOptionValue(opt)) : null);
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
