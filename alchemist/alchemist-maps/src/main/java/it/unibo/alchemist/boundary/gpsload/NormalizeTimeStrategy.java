package it.unibo.alchemist.boundary.gpsload;

import gnu.trove.map.TIntObjectMap;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * Strategy to define how normalize the time of all trace.
 */
public interface NormalizeTimeStrategy {

    /**
     * 
     * @param mapTrace map trace with time to normalize 
     * @return map trace with normalized time
     */
    TIntObjectMap<GPSTrace> normalizeTime(TIntObjectMap<GPSTrace> mapTrace);
}
