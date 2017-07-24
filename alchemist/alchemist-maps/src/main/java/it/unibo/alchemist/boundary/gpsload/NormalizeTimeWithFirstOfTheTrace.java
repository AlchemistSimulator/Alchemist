package it.unibo.alchemist.boundary.gpsload;

import java.util.List;
import java.util.stream.Collectors;

import at.jku.traces.json.GPSTraceImpl;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.model.implementations.positions.GPSPointImpl;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 */
public class NormalizeTimeWithFirstOfTheTrace implements NormalizeTimeStrategy {

    @Override
    public TIntObjectMap<GPSTrace> normalizeTime(final TIntObjectMap<GPSTrace> mapTrace) {
        final TIntObjectMap<GPSTrace> newMapTrace = new TIntObjectHashMap<>();
        for (int i = 0; i < mapTrace.size(); i++) {
            final GPSTrace trace = mapTrace.get(i);
            final Time minTime = getMinTime(trace);
            final List<GPSPoint> newTrace = trace.stream()
                    .map(point -> new GPSPointImpl(point.getLatitude(),
                            point.getLongitude(), point.getTime().subtract(minTime)))
                    .collect(Collectors.toList());
            newMapTrace.put(i, new GPSTraceImpl(newTrace));
        }
        return newMapTrace;
    }

    private Time getMinTime(final GPSTrace trace) {
        final double mintime = trace.stream().map(point -> point.getTime().toDouble())
                .min(Double::compare)
                .get();
        return new DoubleTime(mintime);
    }

}
