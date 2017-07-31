package it.unibo.alchemist.boundary.gpsload.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * no normalize time, return the identical list of {@link GPSTrace} but immutable.
 */
public class NoNormalize implements GPSTimeNormalizer {

    @Override
    public ImmutableList<GPSTrace> normalizeTime(final List<GPSTrace> traces) {
        return traces.stream().collect(ImmutableList.toImmutableList());
    }

}
