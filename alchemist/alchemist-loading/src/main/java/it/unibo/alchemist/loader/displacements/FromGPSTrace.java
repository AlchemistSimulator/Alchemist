/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
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
public class FromGPSTrace implements Displacement {

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
    public FromGPSTrace(final int numNode, final String path, final boolean cycle, final String normalizer, final Object... args) throws IOException {
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
