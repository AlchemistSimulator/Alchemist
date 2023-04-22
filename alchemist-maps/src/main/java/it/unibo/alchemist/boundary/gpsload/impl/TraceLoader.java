/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gpsload.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import it.unibo.alchemist.util.ClassPathScanner;
import it.unibo.alchemist.boundary.gpsload.api.GPSFileLoader;
import it.unibo.alchemist.boundary.gpsload.api.GPSTimeAlignment;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import org.danilopianini.jirf.Factory;
import org.danilopianini.jirf.FactoryBuilder;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jooq.lambda.tuple.Tuple2;
import org.kaikikm.threadresloader.ResourceLoader;
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public final class TraceLoader implements Iterable<GPSTrace> {

    private static final Map<String, GPSFileLoader> LOADER = ClassPathScanner
            .subTypesOf(GPSFileLoader.class, "it.unibo.alchemist")
            .stream()
            .map(Unchecked.function(clazz -> clazz.getConstructor().newInstance()))
            .flatMap(l -> l.supportedExtensions().stream()
                    .map(ext -> new Tuple2<>(ext.toLowerCase(Locale.US), l)))
            .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
    private final boolean cyclic;
    private final ImmutableList<GPSTrace> traces;
    private static final Factory FACTORY = new FactoryBuilder()
        .withWideningConversions()
        .withNarrowingConversions()
        .withAutoBoxing()
        .build();
    private static final List<Class<? extends GPSTimeAlignment>> AVAILABLE_GPS_TIME_ALIGNMENT =
            ClassPathScanner.subTypesOf(GPSTimeAlignment.class);
    private static final Semaphore MUTEX = new Semaphore(1);

    /**
     * 
     * @param path
     *            path with the gps tracks
     * @param cycle
     *            true if considering list of GPSTrace cycle, default false
     * @param normalizer
     *            normalizer to use for normalize time
     * @throws IOException in case of I/O errors
     */
    public TraceLoader(final String path, final boolean cycle, final GPSTimeAlignment normalizer) throws IOException {
        this.cyclic = cycle;
        traces = normalizer.alignTime(loadTraces(path));
    }

    /**
     * 
     * @param path
     *            path with the gps tracks
     * @param cycle
     *            true if considering list of GPSTrace cycle, default false
     * @param timeNormalizerClass
     *            class to use to normalize time
     * @param normalizerArgs
     *            args to use to create GPSTimeNormalizer
     * @throws IOException in case of I/O errors
     */
    public TraceLoader(
        final String path,
        final boolean cycle,
        final String timeNormalizerClass,
        final Object... normalizerArgs
    ) throws IOException {
        this(path, cycle, makeNormalizer(timeNormalizerClass, normalizerArgs));
    }

    /**
     * 
     * @param path
     *            path with the gps tracks
     * @param normalizer
     *            normalizer to use for normalize time
     * @throws IOException in case of I/O errors
     */
    public TraceLoader(final String path, final GPSTimeAlignment normalizer) throws IOException  {
        this(path, false, normalizer);
    }

    /**
     * 
     * @param path
     *            path with the gps tracks
     * @param timeNormalizerClass
     *            class to use to normalize time
     * @param normalizerArgs
     *            args to use to create GPSTimeNormalizer
     * @throws IOException in case of I/O errors
     */
    public TraceLoader(
        final String path,
        final String timeNormalizerClass,
        final Object... normalizerArgs
    ) throws IOException {
        this(path, false, timeNormalizerClass, normalizerArgs);
    }

    @Nonnull
    @Override
    public Iterator<GPSTrace> iterator() {
        return cyclic ? Iterators.cycle(traces) : traces.iterator();
    }

    private List<GPSTrace> loadTraces(final String path) throws IOException {
        /*
         * check if path is a directory or a file
         */
        final boolean isDirectory = runOnPathsStream(path, s -> s.allMatch(line -> ResourceLoader.getResource(line) != null));

        if (isDirectory) {
            /*
             * if path is a directory, call this method recursively on all of its file 
             */
            return runOnPathsStream(path, s -> s
                    .map(CheckedFunction.unchecked(this::loadTraces))
                    .flatMap(List::stream)
                    .collect(ImmutableList.toImmutableList()));
        } else {
            /*
             * If the path is a file, pick its loader by file extension
             */
            final String[] pathSplit = path.split("\\.");
            final String extensionFile = pathSplit[pathSplit.length - 1].toLowerCase(Locale.US);
            if (!LOADER.containsKey(extensionFile)) {
                throw new IllegalArgumentException(
                        "no loader defined for file with extension: " + extensionFile + " (file: " + path + ")"
                );
            }
            final GPSFileLoader fileLoader = LOADER.get(extensionFile);
            try {
                /*
                 * invoke the loader to load all tracks in the file
                 */
                return fileLoader.readTrace(ResourceLoader.getResource(path));
            }  catch (FileFormatException e) {
                throw new IllegalStateException(
                    "Loader: " + LOADER.get(extensionFile).getClass().getSimpleName()
                        + " can't load file: " + path + ", plese make sure it is a " + extensionFile + "file?",
                    e
                );
            } 
        }
    }

    /**
     * 
     * @return the number of traces loaded, Optional.empty() if cyclic
     */
    public Optional<Integer> size() {
        return Optional.ofNullable(cyclic ? null : traces.size());
    }

    private static GPSTimeAlignment makeNormalizer(final String clazzName, final Object... args) {
        final var targetClass = clazzName.contains(".")
            ? findClassWithName(Class::getName, clazzName)
            : findClassWithName(Class::getSimpleName, clazzName);
        if (targetClass.isEmpty()) {
            throw new IllegalArgumentException("Normalizer with claas name: " + clazzName + " not found."
                + "Available GPSTimeAlignment are: [" + AVAILABLE_GPS_TIME_ALIGNMENT.stream()
                    .map(Class::getName)
                    .reduce((c1, c2) -> c1 + ", " + c2)
                    .orElse("")
                + " ]");
        }
        try {
            MUTEX.acquireUninterruptibly();
            final var buildResult = FACTORY.build(targetClass.get(), args);
            return buildResult.getCreatedObjectOrThrowException();
        } finally {
            MUTEX.release();
        }
    }

    private static Optional<Class<? extends GPSTimeAlignment>> findClassWithName(
        final Function<Class<? extends GPSTimeAlignment>, String> classToName, final String targetName) {
        return AVAILABLE_GPS_TIME_ALIGNMENT.stream()
            .filter(clazz -> classToName.apply(clazz).equals(targetName))
            .findFirst();
    }

    private static <R> R runOnPathsStream(final String path, final Function<Stream<String>, R> op) {
        final InputStream resourceStream = ResourceLoader.getResourceAsStream(path);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))) {
            return op.apply(in.lines().map(line -> path + "/" + line));
        } catch (IOException e) {
            throw new IllegalArgumentException("error reading lines of: " + path, e);
        }
    }

}
