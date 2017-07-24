package it.unibo.alchemist.boundary.gpsload;

import java.io.FileNotFoundException;
import java.io.IOException;

import gnu.trove.map.TIntObjectMap;
import it.unibo.alchemist.model.interfaces.GPSTrace;

public interface LoadGPSTraceMappingStrategy {

    TIntObjectMap<GPSTrace> getGPSTraceMapping(String directoryPath) throws FileNotFoundException, IOException;
}
