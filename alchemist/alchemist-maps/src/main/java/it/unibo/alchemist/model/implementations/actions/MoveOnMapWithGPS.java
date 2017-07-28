package it.unibo.alchemist.model.implementations.actions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.danilopianini.util.Hashes;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import it.unibo.alchemist.boundary.gpsload.impl.TraceLoader;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;

/**
 * basic action that follow a {@link GPSTrace}.
 * @param <T>
 */
public class MoveOnMapWithGPS<T> extends MoveOnMap<T> {

    private static final long serialVersionUID = 1L;
//    private static final Map<Environment, >
    private static LoadingCache<TraceRef, TraceLoader> TRACE_LOADER_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(key -> new TraceLoader(key.path, key.cycle, key.normalizer, key.args));
    private static LoadingCache<MapEnvironment<?>, LoadingCache<TraceRef, Iterator<GPSTrace>>> LOADER = Caffeine.newBuilder()
            .weakKeys()
            .build(e -> Caffeine.newBuilder().build(key -> TRACE_LOADER_CACHE.get(key).iterator()));
    private final GPSTrace trace;

    /**
     * 
     * @param environment the environment
     * @param node the node
     * @param rt the {@link RoutingStrategy}
     * @param sp the {@link SpeedSelectionStrategy}
     * @param tg {@link TargetSelectionStrategy}
     * @param trace the {@link GPSTrace} followed
     */
    public MoveOnMapWithGPS(final MapEnvironment<T> environment, final Node<T> node, 
            final RoutingStrategy<T> rt, final SpeedSelectionStrategy<T> sp, 
            final TargetSelectionStrategy<T> tg,
            final String path, final boolean cycle, final String normalizer, final Object... normalizerArgs) {
        super(environment, node, rt, sp, tg);
        final TraceRef key = new TraceRef(path, cycle, normalizer, normalizerArgs);
        final Iterator<GPSTrace> iter = LOADER.get(environment).get(key);
        synchronized (iter) {
            if (iter.hasNext()) {
                this.trace = iter.next();
            } else {
                throw new IllegalStateException("All traces for " + key + " have been consumed.");
            }
        }
    }

    private static class TraceRef {

        final String path, normalizer;
        final boolean cycle;
        final Object[] args;
        int hash;
        
        TraceRef(final String path, final boolean cycle, final String normalizer, final Object... args) {
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

    /**
     * 
     * @return {@link GPSTrace} followed by this action
     */
    protected GPSTrace getTrace() {
        return trace;
    }
}
