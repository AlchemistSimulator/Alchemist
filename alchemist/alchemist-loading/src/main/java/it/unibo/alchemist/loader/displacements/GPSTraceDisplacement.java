package it.unibo.alchemist.loader.displacements;

import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import it.unibo.alchemist.boundary.gpsload.impl.TraceLoader;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Distributes nodes in the first positions of {@link GPSTrace}.
 */
public class GPSTraceDisplacement implements Displacement {

    private final TraceLoader traces;
    private final int numNode;

    /**
     * @param numNode
     *            number of node request
     * @param path
     *            path with the gps tracks
     * @param cycle
     *            true if considering list of GPSTrace cycle, default false
     * @param normalizer
     *            class to use to normalize time
     * @param args
     *            args to use to create GPSTimeNormalizer
     * @throws IOException 
     */
    public GPSTraceDisplacement(final int numNode, final String path, final boolean cycle, final String normalizer, final Object... args) throws IOException {
        traces = new TraceLoader(path, cycle, normalizer, args);
        if (traces.size().map(size -> size < numNode).orElse(false)) {
            throw new IllegalArgumentException(numNode + "traces required, " + traces.size().get() + " traces available");
        }
        this.numNode = numNode;
    }

    @Override
    public Stream<Position> stream() {
        return StreamSupport.stream(traces.spliterator(), false)
                .limit(numNode)
                .map(trace -> trace.getInitialPosition());
    }

}
