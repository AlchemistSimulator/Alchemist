/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.deployments;

import it.unibo.alchemist.boundary.gpsload.impl.TraceLoader;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Position;

import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Distributes nodes in the first positions of {@link it.unibo.alchemist.model.interfaces.GPSTrace}.
 */
public final class FromGPSTrace implements Deployment {

    private final TraceLoader traces;
    private final int numNode;

    /**
     * @param nodeCount
     *            number of node request
     * @param path
     *            path with the gps tracks
     * @param cycle
     *            true if, in case there are more nodes to deploy than available GPS traces,
     *            the traces should be reused cyclically. E.g., if 10 nodes must be deployed
     *            but only 9 GPS traces are available, the first one is reused for the 10th
     *            node.
     * @param normalizer
     *            class to use to normalize time
     * @param args
     *            args to use to create GPSTimeNormalizer
     * @throws IOException if there are errors accessing the file system
     */
    public FromGPSTrace(
            final int nodeCount,
            final String path,
            final boolean cycle,
            final String normalizer,
            final Object... args
    ) throws IOException {
        traces = new TraceLoader(path, cycle, normalizer, args);
        if (traces.size().map(size -> size < nodeCount).orElse(false)) {
            throw new IllegalArgumentException(
                    nodeCount + "traces required, " + traces.size().orElse(-1) + " traces available"
            );
        }
        this.numNode = nodeCount;
    }

    @Override
    public Stream<Position> stream() {
        return StreamSupport.stream(traces.spliterator(), false)
                .limit(numNode)
                .map(GPSTrace::getInitialPosition);
    }

}
