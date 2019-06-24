/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.shapes.GHPoint;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.implementations.routes.GraphHopperRoute;
import it.unibo.alchemist.model.implementations.routes.PolygonalChain;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Route;
import it.unibo.alchemist.model.interfaces.Vehicle;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.danilopianini.util.Hashes;
import org.danilopianini.util.concurrent.FastReadWriteLock;
import org.jetbrains.annotations.NotNull;
import org.jooq.lambda.Unchecked;
import org.kaikikm.threadresloader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * This class serves as template for more specific implementations of
 * environments using a map. It encloses the navigation logic, but leaves the
 * subclasses to decide how to provide map data (e.g. loading from disk or rely
 * on online services). The data is then stored in-memory for performance
 * reasons.
 *
 * @param <T>
 */
public final class OSMEnvironment<T> extends Abstract2DEnvironment<T, GeoPosition> implements MapEnvironment<T> {

    /**
     * The default routing algorithm.
     */
    public static final String DEFAULT_ALGORITHM = "dijkstrabi";

    /**
     * The default routing strategy.
     */
    public static final String ROUTING_STRATEGY = "fastest";

    /**
     * The default value for approximating the positions comparison.
     */
    public static final int DEFAULT_APPROXIMATION = 0;

    /**
     * The default value for the force nodes on streets option.
     */
    public static final boolean DEFAULT_ON_STREETS = true;

    /**
     * The default value for the discard of nodes too far from streets option.
     */
    public static final boolean DEFAULT_FORCE_STREETS = false;
    private static final Logger L = LoggerFactory.getLogger(OSMEnvironment.class);
    private static final long serialVersionUID = 1L;
    /**
     * System file separator.
     */
    private static final String SLASH = System.getProperty("file.separator");
    /**
     * Alchemist's temp dir.
     */
    private static final String PERSISTENTPATH = System.getProperty("user.home") + SLASH + ".alchemist";
    private static final String MAPNAME = "map";
    private static final Semaphore LOCK_FILE = new Semaphore(1);
    private final String mapResource;
    private final boolean forceStreets, onlyStreet;
    private transient FastReadWriteLock mapLock;
    private transient Map<Vehicle, GraphHopperAPI> navigators;
    private transient LoadingCache<CacheEntry, Route<GeoPosition>> routecache;
    private boolean benchmarking;
    private final int approximation;



    /**
     * Builds a new {@link OSMEnvironment}, with nodes forced on streets.
     *
     * @param file
     *            the file path where the map data is stored
     * @throws IOException
     *             if the map file is not found, or it's not readable, or
     *             accessible, or a file system error occurred, or you kicked your
     *             hard drive while Alchemist was reading the map
     */
    public OSMEnvironment(final String file) throws IOException {
        this(file, DEFAULT_ON_STREETS);
    }

    /**
     * @param file
     *            the file path where the map data is stored
     * @param onStreets
     *            if true, the nodes will be placed on the street nearest to the
     *            desired {@link it.unibo.alchemist.model.interfaces.Position}.
     * @throws IOException
     *             if the map file is not found, or it's not readable, or
     *             accessible, or a file system error occurred, or you kicked your
     *             hard drive while Alchemist was reading the map
     */
    public OSMEnvironment(final String file, final boolean onStreets) throws IOException {
        this(file, onStreets, DEFAULT_FORCE_STREETS);
    }

    /**
     * @param file
     *            the file path where the map data is stored
     * @param onStreets
     *            if true, the nodes will be placed on the street nearest to the
     *            desired {@link it.unibo.alchemist.model.interfaces.Position}.
     * @param onlyOnStreets
     *            if true, the nodes which are too far from a street will be simply
     *            discarded. If false, they will be placed anyway, in the original
     *            position.
     * @throws IOException
     *             if the map file is not found, or it's not readable, or
     *             accessible, or a file system error occurred, or you kicked your
     *             hard drive while Alchemist was reading the map
     */
    public OSMEnvironment(final String file, final boolean onStreets, final boolean onlyOnStreets) throws IOException {
        this(file, DEFAULT_APPROXIMATION, onStreets, onlyOnStreets);

    }

    /**
     * @param file
     *            the file path where the map data is stored. Accepts OSM maps of
     *            any format (xml, osm, pbf). The map will be processed, optimized
     *            and stored for future use.
     * @param approximation
     *            the amount of ciphers of the IEEE 754 encoded
     *            position that may be discarded when comparing two positions,
     *            allowing a quicker retrieval of the route between two position,
     *            since the cache may already contain a similar route  which
     *            can be considered to be the same route, according to
     *            the level of precision determined by this value
     * @throws IOException
     *             if the map file is not found, or it's not readable, or
     *             accessible, or a file system error occurred, or you kicked
     *             your hard drive while Alchemist was reading the map
     */
    public OSMEnvironment(final String file, final int approximation) throws IOException {
        this(file, approximation, DEFAULT_ON_STREETS, DEFAULT_FORCE_STREETS);
    }

    /**
     * @param file
     *            the file path where the map data is stored. Accepts OSM maps of
     *            any format (xml, osm, pbf). The map will be processed, optimized
     *            and stored for future use.
     * @param approximation
     *            the amount of ciphers of the IEEE 754 encoded
     *            position that may be discarded when comparing two positions,
     *            allowing a quicker retrieval of the route between two position,
     *            since the cache may already contain a similar route  which
     *            can be considered to be the same route, according to
     *            the level of precision determined by this value
     * @param onStreets
     *            if true, the nodes will be placed on the street nearest to the
     *            desired {@link it.unibo.alchemist.model.interfaces.Position}.
     * @param onlyOnStreets
     *            if true, the nodes which are too far from a street will be simply
     *            discarded. If false, they will be placed anyway, in the original
     *            position.
     * @throws IOException
     *             if the map file is not found, or it's not readable, or
     *             accessible, or a file system error occurred, or you kicked your
     *             hard drive while Alchemist was reading the map
     */
    public OSMEnvironment(final String file, final int approximation, final boolean onStreets, final boolean onlyOnStreets) throws IOException {
        super();
        if (approximation < 0 || approximation > 64) {
            throw new IllegalArgumentException();
        }
        forceStreets = onStreets;
        onlyStreet = onlyOnStreets;
        mapResource = file;
        this.approximation = approximation;
        initAll(file);
    }

    private boolean directoryIsReadOnly(final String dir) {
        return !new File(dir).canWrite();
    }

    @Override
    protected GeoPosition computeActualInsertionPosition(final Node<T> node, final GeoPosition position) {
        /*
         * If it must be located on streets, query the navigation engine for a street
         * point. Otherwise, put it where it is declared.
         */
        assert position != null;
        return forceStreets ? getNearestStreetPoint(position).orElse(position) : position;
    }

    @Override
    public Route<GeoPosition> computeRoute(final GeoPosition p1, final GeoPosition p2) {
        return computeRoute(p1, p2, DEFAULT_VEHICLE);
    }

    @Override
    public Route<GeoPosition> computeRoute(final GeoPosition p1, final GeoPosition p2, final Vehicle vehicle) {
        if (routecache == null) {
            final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
            if (benchmarking) {
                builder.recordStats();
            }
            routecache = builder
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .build(new CacheLoader<CacheEntry, Route<GeoPosition>>() {
                    @Override
                    public Route<GeoPosition> load(@NotNull final CacheEntry key) {
                        final Vehicle vehicle = Objects.requireNonNull(key).v;
                        final GeoPosition p1 = key.start;
                        final GeoPosition p2 = key.end;
                        final GHRequest req = new GHRequest(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude())
                                .setAlgorithm(DEFAULT_ALGORITHM)
                                .setVehicle(vehicle.toString())
                                .setWeighting(ROUTING_STRATEGY);
                        mapLock.read();
                        final GraphHopperAPI gh = navigators.get(vehicle);
                        mapLock.release();
                        if (gh != null) {
                            final GHResponse resp = gh.route(req);
                            if (resp.getErrors().isEmpty()) {
                                return new GraphHopperRoute(resp);
                            } else {
                                return new PolygonalChain<>(p1, p2);
                            }
                        }
                        throw new IllegalStateException("Something went wrong while evaluating a route.");
                    }
                });
        }
        try {
            return routecache.get(new CacheEntry(vehicle, p1, p2));
        } catch (ExecutionException e) {
            L.error("", e);
            throw new IllegalStateException("The navigator was unable to compute a route from " + p1 + " to " + p2
                    + " using the navigator " + vehicle + ". This is most likely a bug", e);
        }
    }

    @Override
    public Route<GeoPosition> computeRoute(final Node<T> node, final GeoPosition coord) {
        return computeRoute(node, coord, DEFAULT_VEHICLE);
    }

    @Override
    public Route<GeoPosition> computeRoute(final Node<T> node, final GeoPosition coord, final Vehicle vehicle) {
        return computeRoute(getPosition(node), coord, vehicle);
    }

    @Override
    public Route<GeoPosition> computeRoute(final Node<T> node, final Node<T> node2) {
        return computeRoute(node, getPosition(node2));
    }

    @Override
    public void enableBenchmark() {
        this.benchmarking = true;
    }

    @Override
    public double getBenchmarkResult() {
        if (benchmarking) {
            if (routecache != null) {
                return routecache.stats().hitRate();
            }
            return 0;
        } else {
            throw new IllegalStateException("You should call doBenchmark() before.");
        }
    }

    /**
     * @return the maximum latitude
     */
    protected double getMaxLatitude() {
        return super.getOffset()[1] + super.getSize()[1];
    }

    /**
     * @return the maximum longitude
     */
    protected double getMaxLongitude() {
        return super.getOffset()[0] + super.getSize()[0];
    }

    /**
     * @return the minimum latitude
     */
    protected double getMinLatitude() {
        return super.getOffset()[1];
    }

    /**
     * @return the minimum longitude
     */
    protected double getMinLongitude() {
        return super.getOffset()[0];
    }

    private Optional<GeoPosition> getNearestStreetPoint(final GeoPosition position) {
        assert position != null;
        mapLock.read();
        final GraphHopperAPI gh = navigators.get(Vehicle.BIKE);
        mapLock.release();
        final QueryResult qr = ((GraphHopper) gh).getLocationIndex()
                .findClosest(position.getLatitude(), position.getLongitude(), EdgeFilter.ALL_EDGES);
        if (qr.isValid()) {
            final GHPoint pt = qr.getSnappedPoint();
            return Optional.of(new LatLongPosition(pt.lat, pt.lon));
        }
        return Optional.empty();
    }

    @Override
    public double[] getSizeInDistanceUnits() {
        final double minlat = getMinLatitude();
        final double maxlat = getMaxLatitude();
        final double minlon = getMinLongitude();
        final double maxlon = getMaxLongitude();
        final GeoPosition minmin = new LatLongPosition(minlat, minlon);
        final GeoPosition minmax = new LatLongPosition(minlat, maxlon);
        final GeoPosition maxmin = new LatLongPosition(maxlat, minlon);
        final GeoPosition maxmax = new LatLongPosition(maxlat, maxlon);
        /*
         * Maximum x: maximum distance between the same longitudes Maximum y: maximum
         * distance between the same latitudes
         */
        final double sizex = Math.max(minmin.getDistanceTo(minmax), maxmax.getDistanceTo(maxmin));
        final double sizey = Math.max(minmin.getDistanceTo(maxmin), maxmax.getDistanceTo(minmax));
        return new double[] { sizex, sizey };
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private void initAll(final String fileName) throws IOException {
        Objects.requireNonNull(fileName, "define the file with the map: " + fileName);
        final Optional<URL> file = Optional.of(new File(fileName))
                .filter(File::exists)
                .map(File::toURI)
                .map(Unchecked.function(URI::toURL));
        final URL resource = Optional.ofNullable(ResourceLoader.getResource(fileName))
            .orElseGet(Unchecked.supplier(() ->
                file.orElseThrow(() -> new FileNotFoundException("No file or resource with name " + fileName))));
        final String dir = initDir(resource).intern();
        final File workdir = new File(dir);
        mkdirsIfNeeded(workdir);
        final File mapFile = new File(dir + SLASH + MAPNAME);
        LOCK_FILE.acquireUninterruptibly();
        try (RandomAccessFile fileAccess = new RandomAccessFile(workdir + SLASH + "lock", "rw")) {
            try (FileLock lock = fileAccess.getChannel().lock()) {
                if (!mapFile.exists()) {
                    Files.copy(resource.openStream(), mapFile.toPath());
                }
                lock.release();
            }
        } finally {
            LOCK_FILE.release();
        }
        navigators = new EnumMap<>(Vehicle.class);
        mapLock = new FastReadWriteLock();
        final Optional<Exception> error = Arrays.stream(Vehicle.values()).parallel().<Optional<Exception>>map(v -> {
            try {
                final String internalWorkdir = workdir + SLASH + v;
                final File iwdf = new File(internalWorkdir);
                if (mkdirsIfNeeded(iwdf)) {
                    final GraphHopperAPI gh = initNavigationSystem(mapFile, internalWorkdir, v);
                    mapLock.write();
                    navigators.put(v, gh);
                    mapLock.release();
                }
                return Optional.empty();
            } catch (Exception e) { // NOPMD AvoidCatchingGenericException
                return Optional.of(e);
            }
        }).filter(Optional::isPresent).map(Optional::get).findFirst();
        if (error.isPresent()) {
            throw new IllegalStateException("A error occurred during initialization.", error.get());
        }
    }

    private String initDir(final URL mapfile) throws IOException {
        final String code = Hex.encodeHexString(Hashing.sha256().hashBytes(IOUtils.toByteArray(mapfile.openStream())).asBytes());
        final String append = SLASH + code;
        final String[] prefixes = new String[] { PERSISTENTPATH, System.getProperty("java.io.tmpdir"),
                System.getProperty("user.dir"), "." };
        String dir = prefixes[0] + append;
        for (int i = 1; (!mkdirsIfNeeded(dir) || directoryIsReadOnly(dir)) && i < prefixes.length; i++) {
            L.warn("Can not write on " + dir + ", trying " + prefixes[i]);
            dir = prefixes[i] + append;
        }
        if (directoryIsReadOnly(dir)) {
            /*
             * Give up.
             */
            throw new IOException(
                    "None of: " + Arrays.toString(prefixes) + " is writeable. I can not initialize GraphHopper cache.");
        }
        return dir;
    }

    @Override
    public GeoPosition makePosition(final Number... coordinates) {
        if (coordinates.length != 2) {
            throw new IllegalArgumentException(
                    getClass().getSimpleName() + " only supports bi-dimensional coordinates (latitude, longitude)");
        }
        return new LatLongPosition(coordinates[0].doubleValue(), coordinates[1].doubleValue());
    }

    /**
     * There is a single case in which nodes are discarded: if there are no traces
     * for this node and nodes are required to lay on streets, but the navigation
     * engine can not resolve any such position.
     */
    @Override
    protected boolean nodeShouldBeAdded(final Node<T> node, final GeoPosition position) {
        assert node != null;
        return !onlyStreet || getNearestStreetPoint(position).isPresent();
    }

    private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        initAll(mapResource);
    }

    private static synchronized GraphHopperAPI initNavigationSystem(final File mapFile, final String internalWorkdir, final Vehicle v) {
        return new GraphHopperOSM().setOSMFile(mapFile.getAbsolutePath()).forDesktop()
                .setElevation(false)
                .setEnableCalcPoints(true)
                .setInMemory()
                .setGraphHopperLocation(internalWorkdir)
                .setEncodingManager(EncodingManager.create(v.toString().toLowerCase(Locale.US)))
                .importOrLoad();
    }

    private static boolean mkdirsIfNeeded(final File target) {
        return target.exists() || target.mkdirs();
    }

    private static boolean mkdirsIfNeeded(final String target) {
        return mkdirsIfNeeded(new File(target));
    }

    private final class CacheEntry {

        private final GeoPosition apprEnd;
        private final GeoPosition apprStart;
        private final GeoPosition end;
        private int hash;
        private final GeoPosition start;
        private final Vehicle v;

        private CacheEntry(final Vehicle v, final GeoPosition p1, final GeoPosition p2) {
            this.v = Objects.requireNonNull(v);
            this.start = Objects.requireNonNull(p1);
            this.end = Objects.requireNonNull(p2);
            this.apprStart = approximate(start);
            this.apprEnd = approximate(end);
        }

        private double approximate(final double value) {
            return Double.longBitsToDouble(Double.doubleToLongBits(value) & (0xFFFFFFFFFFFFFFFFL << approximation));
        }

        private GeoPosition approximate(final GeoPosition p) {
            if (approximation == 0) {
                return p;
            }
            return makePosition(approximate(p.getLatitude()), approximate(p.getLongitude()));
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof OSMEnvironment.CacheEntry) {
                final OSMEnvironment<?>.CacheEntry other = (OSMEnvironment<?>.CacheEntry) obj;
                return v.equals(other.v) && apprStart.equals(other.apprStart) && apprEnd.equals(other.apprEnd);
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash =  Hashes.hash32(v, apprStart, apprEnd);
            }
            return hash;
        }
    }
}
