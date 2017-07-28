package it.unibo.alchemist.boundary.gpsload.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jooq.lambda.tuple.Tuple2;
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;
import org.reflections.Reflections;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import it.unibo.alchemist.boundary.gpsload.api.GPSFileLoader;
import it.unibo.alchemist.boundary.gpsload.api.GPSTimeNormalizer;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * 
 */
public class TraceLoader implements Iterable<GPSTrace> {

    private final ImmutableList<GPSTrace> traces;
    private final boolean cyclic;
    private static final Map<String, GPSFileLoader> LOADER = new Reflections()
            .getSubTypesOf(GPSFileLoader.class).stream()
            .map(Unchecked.function(Class::newInstance))
            .flatMap(l -> l.supportedExtensions().stream()
                    .map(ext -> new Tuple2<>(ext.toLowerCase(Locale.US), l)))
            .collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));

    /**
     * 
     * @param path
     *            path with the gps tracks
     * @param timeNormalizerClass
     *            class to use to normalize time
     * @param normalizerArgs
     *            args to use to create GPSTimeNormalizer
     * @throws IOException 
     */
    public TraceLoader(final String path,
            final String timeNormalizerClass,
            final Object... normalizerArgs) throws IOException {
        this(path, false, timeNormalizerClass, normalizerArgs);
    }

    /**
     * 
     * @param path
     *            path with the gps tracks
     * @param normalizer
     *            normalizer to use for normalize time
     * @throws IOException 
     */
    public TraceLoader(final String path, final GPSTimeNormalizer normalizer) throws IOException  {
        this(path, false, normalizer);
    }

    /**
     * 
     * @param path
     *            path with the gps tracks
     * @param cycle
     *            true if considering list of GPSTrace cycle, default false
     * @param normalizer
     *            normalizer to use for normalize time
     * @throws IOException 
     */
    public TraceLoader(final String path, final boolean cycle, final GPSTimeNormalizer normalizer) throws IOException {
        this.cyclic = cycle;
        traces = normalizer.normalizeTime(loadTraces(path));
    }

    private static GPSTimeNormalizer makeNormalizer(final String clazzName, final Object... args) {
        final String fullName = clazzName.contains(".") ? clazzName : GPSTimeNormalizer.class.getPackage().getName() + "." + clazzName;
        try {
            return Arrays.stream(Class.forName(fullName).getConstructors())
                .map(c -> {
                    try {
                        return Optional.of((GPSTimeNormalizer) c.newInstance(args));
                    } catch (Exception e) {
                        return Optional.<GPSTimeNormalizer>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't instance " + fullName + "(from " + clazzName + ") using " + Arrays.toString(args)));
        } catch (IllegalArgumentException | SecurityException | ClassNotFoundException e) {
            throw new IllegalStateException("Cannot instance or use the GPS time normalizer", e);
        }
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
     * @throws IOException 
     */
    public TraceLoader(final String path,
            final boolean cycle,
            final String timeNormalizerClass,
            final Object... normalizerArgs) throws IOException {
        this(path, cycle, makeNormalizer(timeNormalizerClass, normalizerArgs));
    }

    private List<GPSTrace> loadTraces(final String path) throws IOException {
        final GPSFileLoader fileLoader;
        final List<String> paths;
        final boolean isDirectory;
        /*
         * read all line, if is a directory re-call it with this path
         */
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                TraceLoader.class.getResourceAsStream(path)))) {
            /*
             * read all line of the path
             */
            paths = in.lines()
                    .map(line -> path + "/" + line)
                    .collect(Collectors.toList());
            /*
             * check if path is a directory or a file
             */
            isDirectory = paths.stream().allMatch(line -> TraceLoader.class.getResource(line) != null);

            if (isDirectory) {
                /*
                 * if path is a directory, call this method recursively on all of its file 
                 */
                return paths.stream()
                        .map(CheckedFunction.unchecked(this::loadTraces))
                        .flatMap(List::stream)
                        .collect(ImmutableList.toImmutableList());
                } else {
                /*
                 * If the path is a file, pick its loader by file extension
                 */
                final String[] pathSplit = path.split("\\.");
                final String extensionFile = pathSplit[pathSplit.length - 1].toLowerCase(Locale.US);
                if (!LOADER.containsKey(extensionFile)) {
                    throw new IllegalArgumentException("no loader defined for file with extension: " 
                                                        + extensionFile + " (file: " + path + ")");
                }
                fileLoader = LOADER.get(extensionFile);
                try {
                    /*
                     * invoke the loader to load all trake in the file
                     */
                    return fileLoader.readTrace(TraceLoader.class.getResource(path));
                }  catch (FileFormatException e) {
                    throw new IllegalStateException("the loader: " + LOADER.get(extensionFile).getClass().getSimpleName() + " can't load the file: " + path + ", sure is a " + extensionFile + "file?", e);
                } 
            }
        } catch (IOException e) {
            throw new IOException("error reading lines of: " + path);
        }
    }

    @Override
    public Iterator<GPSTrace> iterator() {
        return cyclic ? Iterators.cycle(traces) : traces.iterator();
    }

}
