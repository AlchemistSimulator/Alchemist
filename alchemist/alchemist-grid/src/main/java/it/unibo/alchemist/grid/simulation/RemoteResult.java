package it.unibo.alchemist.grid.simulation;

import java.io.FileNotFoundException;

import it.unibo.alchemist.grid.exceptions.RemoteSimulationException;

/**
 * Result of {@link RemoteSimulation}.
 *
 */
public interface RemoteResult {
    /**
     * Save simulation's result in a local file.
     * @param targetFile Local file
     * @throws FileNotFoundException 
     * @throws RemoteSimulationException
     */
    void saveLocally(String targetFile) throws FileNotFoundException, RemoteSimulationException;
}
