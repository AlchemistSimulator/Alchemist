/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.implementations.routingservices.GraphHopperOptions;
import it.unibo.alchemist.model.implementations.routingservices.GraphHopperRoutingService;
import it.unibo.alchemist.model.GeoPosition;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Route;
import org.jooq.lambda.Unchecked;
import org.kaikikm.threadresloader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This class serves as template for more specific implementations of
 * environments using a map. It encloses the navigation logic, but leaves the
 * subclasses to decide how to provide map data (e.g. loading from disk or rely
 * on online services). The data is then stored in-memory for performance
 * reasons.
 *
 * @param <T> concentration type
 */
public final class OSMEnvironment<T>
    extends Abstract2DEnvironment<T, GeoPosition>
    implements MapEnvironment<T, GraphHopperOptions, GraphHopperRoutingService> {

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
     * Alchemist's temp dir.
     */
    private final boolean forceStreets, onlyStreet;
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Re-loaded automatically")
    @Nullable private transient GraphHopperRoutingService navigator;
    @Nullable private transient LoadingCache<CacheEntry, Route<GeoPosition>> routecache;
    private boolean benchmarking;
    private final int approximation;
    private final String mapFile;


    /**
     * Builds a new {@link OSMEnvironment} without an actual backing map.
     * This environment will be unable to use the navigation system.
     *
     * @param incarnation the incarnation to be used.
     */
    public OSMEnvironment(final Incarnation<T, GeoPosition> incarnation) {
        this(incarnation, null, false);
    }

    /**
     * Builds a new {@link OSMEnvironment}, with nodes not forced on streets.
     * @param incarnation the incarnation to be used.
     * @param file
     *            the file path where the map data is stored
     */
    public OSMEnvironment(final Incarnation<T, GeoPosition> incarnation, final String file) {
        this(incarnation, file, DEFAULT_ON_STREETS);
    }

    /**
     * @param incarnation the incarnation to be used.
     * @param file
     *            the file path where the map data is stored
     * @param onStreets
     *            if true, the nodes will be placed on the street nearest to the
     *            desired {@link Position}.
     */
    public OSMEnvironment(final Incarnation<T, GeoPosition> incarnation, final String file, final boolean onStreets) {
        this(incarnation, file, onStreets, DEFAULT_FORCE_STREETS);
    }

    /**
     * @param incarnation the incarnation to be used.
     * @param file
     *            the file path where the map data is stored
     * @param onStreets
     *            if true, the nodes will be placed on the street nearest to the
     *            desired {@link Position}.
     * @param onlyOnStreets
     *            if true, the nodes which are too far from a street will be simply
     *            discarded. If false, they will be placed anyway, in the original
     *            position.
     */
    public OSMEnvironment(
        final Incarnation<T, GeoPosition> incarnation,
        final String file,
        final boolean onStreets,
        final boolean onlyOnStreets
    ) {
        this(incarnation, file, DEFAULT_APPROXIMATION, onStreets, onlyOnStreets);
    }

    /**
     * @param incarnation the incarnation to be used.
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
     */
    public OSMEnvironment(final Incarnation<T, GeoPosition> incarnation, final String file, final int approximation) {
        this(incarnation, file, approximation, DEFAULT_ON_STREETS, DEFAULT_FORCE_STREETS);
    }

    /**
     * @param incarnation the incarnation to be used.
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
     *            desired {@link Position}.
     * @param onlyOnStreets
     *            if true, the nodes which are too far from a street will be simply
     *            discarded. If false, they will be placed anyway, in the original
     *            position.
     */
    public OSMEnvironment(
            final Incarnation<T, GeoPosition> incarnation,
            final String file,
            final int approximation,
            final boolean onStreets,
            final boolean onlyOnStreets
    ) {
        super(incarnation);
        if (approximation < 0 || approximation > 64) {
            throw new IllegalArgumentException();
        }
        forceStreets = onStreets;
        onlyStreet = onlyOnStreets;
        if (file == null) {
            L.warn("No OpenStreetMap extract provided. The navigation system won't be available.");
        }
        mapFile = file;
        this.approximation = approximation;
    }

    @Override
    protected GeoPosition computeActualInsertionPosition(final Node<T> node, final GeoPosition position) {
        /*
         * If it must be located on streets, query the navigation engine for a street
         * point. Otherwise, put it where it is declared.
         */
        return forceStreets
            ? Optional.ofNullable(getNavigator().allowedPointClosestTo(Objects.requireNonNull(position)))
                .orElse(position)
            : position;
    }

    @Override
    public Route<GeoPosition> computeRoute(final GeoPosition p1, final GeoPosition p2) {
        return computeRoute(p1, p2, GraphHopperRoutingService.Companion.getDefaultOptions());
    }

    @Override
    public Route<GeoPosition> computeRoute(
        final GeoPosition from,
        final GeoPosition to,
        final GraphHopperOptions options
    ) {
        if (routecache == null) {
            final Caffeine<Object, Object> builder = Caffeine.newBuilder();
            if (benchmarking) {
                builder.recordStats();
            }
            routecache = builder
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .build(key -> getNavigator().route(Objects.requireNonNull(key).start, key.end, key.options));
        }
        return routecache.get(new CacheEntry(options, from, to));
    }

    @Override
    public Route<GeoPosition> computeRoute(final Node<T> node, final GeoPosition coord) {
        return computeRoute(node, coord, GraphHopperRoutingService.Companion.getDefaultOptions());
    }

    @Override
    public Route<GeoPosition> computeRoute(
        final Node<T> node,
        final GeoPosition coord,
        final GraphHopperOptions options
    ) {
        return computeRoute(getPosition(node), coord, options);
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
    private double getMaxLatitude() {
        return super.getOffset()[1] + super.getSize()[1];
    }

    /**
     * @return the maximum longitude
     */
    private double getMaxLongitude() {
        return super.getOffset()[0] + super.getSize()[0];
    }

    /**
     * @return the minimum latitude
     */
    private double getMinLatitude() {
        return super.getOffset()[1];
    }

    /**
     * @return the minimum longitude
     */
    private double getMinLongitude() {
        return super.getOffset()[0];
    }

    @Override
    public GraphHopperRoutingService getRoutingService() {
        return getNavigator();
    }

    @Override
    public double[] getSizeInDistanceUnits() {
        final double minlat = getMinLatitude();
        final double maxlat = getMaxLatitude();
        final double minlon = getMinLongitude();
        final double maxlon = getMaxLongitude();
        if (Double.isNaN(minlat)
                || Double.isNaN(maxlat)
                || Double.isNaN(minlon)
                || Double.isNaN(maxlon)) {
            return new double[] { Double.NaN, Double.NaN };
        }
        final GeoPosition minmin = new LatLongPosition(minlat, minlon);
        final GeoPosition minmax = new LatLongPosition(minlat, maxlon);
        final GeoPosition maxmin = new LatLongPosition(maxlat, minlon);
        final GeoPosition maxmax = new LatLongPosition(maxlat, maxlon);
        /*
         * Maximum x: maximum distance between the same longitudes Maximum y: maximum
         * distance between the same latitudes
         */
        final double sizex = Math.max(minmin.distanceTo(minmax), maxmax.distanceTo(maxmin));
        final double sizey = Math.max(minmin.distanceTo(maxmin), maxmax.distanceTo(minmax));
        return new double[] { sizex, sizey };
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
        return !onlyStreet || getNavigator().allowedPointClosestTo(position) != null;
    }

    @Nonnull
    private GraphHopperRoutingService getNavigator() {
        if (navigator == null) {
            if (mapFile == null) {
                throw new IllegalStateException(
                    "Navigation system required, but no map file has been provided."
                        + " Consider using BBBike at https://extract.bbbike.org/"
                        + "to get a protocolbuffer extract of OpenStreetMap"
                );
            }
            final var map = Optional.of(new File(mapFile))
                .filter(File::exists)
                .map(File::toURI)
                .map(Unchecked.function(URI::toURL))
                .orElse(ResourceLoader.getResource(mapFile));
            if (map == null) {
                throw new IllegalArgumentException(
                    mapFile + " is not a valid file on the file system,"
                        + "nor it can be loaded from the classpath as a Resource"
                );
            }
            navigator = new GraphHopperRoutingService(map);
        }
        return navigator;
    }


    private final class CacheEntry {

        private final GeoPosition apprEnd;
        private final GeoPosition apprStart;
        private final GeoPosition end;
        private int hash;
        private final GeoPosition start;
        private final GraphHopperOptions options;

        private CacheEntry(final GraphHopperOptions options, final GeoPosition from, final GeoPosition to) {
            this.options = Objects.requireNonNull(options);
            this.start = Objects.requireNonNull(from);
            this.end = Objects.requireNonNull(to);
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
                return options.equals(other.options) && apprStart.equals(other.apprStart) && apprEnd.equals(other.apprEnd);
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = Objects.hash(options, apprStart, apprEnd);
            }
            return hash;
        }
    }
}
