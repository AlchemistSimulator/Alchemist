/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.danilopianini.util.Hashes;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.gpsload.impl.TraceLoader;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.ObjectWithGPS;
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;

import static java.util.Objects.requireNonNull;

/**
 * basic action that follow a {@link GPSTrace}.
 * @param <T>
 */
@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "SpotBugs is reporting false positives")
public class MoveOnMapWithGPS<T> extends MoveOnMap<T> {

    private static final long serialVersionUID = 1L;
    private static final LoadingCache<TraceRef, TraceLoader> TRACE_LOADER_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(key -> new TraceLoader(key.path, key.cycle, key.normalizer, key.args));
    private static final LoadingCache<MapEnvironment<?>, LoadingCache<TraceRef, Iterator<GPSTrace>>> LOADER = Caffeine.newBuilder()
            .weakKeys()
            .build(e -> Caffeine.newBuilder().build(key -> Objects.requireNonNull(TRACE_LOADER_CACHE.get(key)).iterator()));
    private final GPSTrace trace;

    /**
     * 
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param rt
     *            the {@link RoutingStrategy}
     * @param sp
     *            the {@link SpeedSelectionStrategy}
     * @param tg
     *            {@link TargetSelectionStrategy}
     * @param path
     *            resource(file, directory, ...) with GPS trace
     * @param cycle
     *            true if the traces have to be distributed cyclically
     * @param normalizer
     *            name of the class that implement the strategy to normalize the
     *            time
     * @param normalizerArgs
     *            Args to build normalize
     */
    public MoveOnMapWithGPS(final MapEnvironment<T> environment,
            final Node<T> node, 
            final RoutingStrategy<GeoPosition> rt,
            final SpeedSelectionStrategy<GeoPosition> sp, 
            final TargetSelectionStrategy<GeoPosition> tg,
            final String path, final boolean cycle, final String normalizer, final Object... normalizerArgs) {
        this(environment, node, rt, sp, tg, traceFor(environment, path, cycle, normalizer, normalizerArgs));
    }

    /**
     * 
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param rt
     *            the {@link RoutingStrategy}
     * @param sp
     *            the {@link SpeedSelectionStrategy}
     * @param tg
     *            {@link TargetSelectionStrategy}
     * @param trace
     *            {@link GPSTrace to follow}
     */
    public MoveOnMapWithGPS(
            final MapEnvironment<T> environment,
            final Node<T> node, 
            final RoutingStrategy<GeoPosition> rt,
            final SpeedSelectionStrategy<GeoPosition> sp, 
            final TargetSelectionStrategy<GeoPosition> tg,
            final GPSTrace trace) {
        super(environment, node, rt, sp, tg);
        this.trace = requireNonNull(trace);
        if (rt instanceof ObjectWithGPS) {
            ((ObjectWithGPS) rt).setTrace(trace);
        }
        if (sp instanceof ObjectWithGPS) {
            ((ObjectWithGPS) sp).setTrace(trace);
        }
        if (tg instanceof ObjectWithGPS) {
            ((ObjectWithGPS) tg).setTrace(trace);
        }
    }

    /**
     * 
     * @param environment
     *            the environment
     * @param path
     *            resource(file, directory, ...) with GPS trace
     * @param cycle
     *            true if the traces have to be distributed cyclically
     * @param normalizer
     *            name of the class that implement the strategy to normalize the
     *            time
     * @param normalizerArgs
     *            Args to build normalize
     * @return the GPSTrace
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static GPSTrace traceFor(final MapEnvironment<?> environment, final String path, final boolean cycle, final String normalizer, final Object... normalizerArgs) {
        final LoadingCache<TraceRef, Iterator<GPSTrace>> gpsTraceLoader = LOADER.get(requireNonNull(environment));
        if (gpsTraceLoader == null) {
            throw new IllegalStateException("Unable to load a GPS Trace mapping for: " + environment + " (null was returned)");
        }
        final TraceRef key = new TraceRef(requireNonNull(path), cycle, normalizer, requireNonNull(normalizerArgs));
        final Iterator<GPSTrace> iter = gpsTraceLoader.get(key);
        if (iter == null) {
            throw new IllegalStateException("Unable to load a GPS Trace iterator for: " + key);
        }
        synchronized (iter) {
            if (iter.hasNext()) {
                return iter.next();
            } else {
                throw new IllegalStateException("All traces for " + key + " have been consumed.");
            }
        }
    }

    /**
     * 
     * @return {@link GPSTrace} followed by this action
     */
    protected GPSTrace getTrace() {
        return trace;
    }

    private static class TraceRef {

        private final String path, normalizer;
        private final boolean cycle;
        private final Object[] args;
        private int hash;

        TraceRef(final String path,
                 final boolean cycle,
                 final String normalizer,
                 final Object... args) { // NOPMD: array is stored direcly by purpose.
            this.path = path;
            this.cycle = cycle;
            this.normalizer = normalizer;
            this.args = args;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = Hashes.hash32(path, normalizer, cycle, args);
            }
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof TraceRef) {
                final TraceRef ck = (TraceRef) obj;
                return path.equals(ck.path)
                        && normalizer.equals(ck.normalizer)
                        && cycle == ck.cycle
                        && Arrays.deepEquals(args, ck.args);
            }
            return false;
        }

        @Override
        public String toString() {
            return (cycle ? "Cyclic" : "")
                    + "Trace[path=" + path
                    + ", normalizer=" + normalizer
                    + "(" + Arrays.toString(args) + ")]";
        }

    }
}
