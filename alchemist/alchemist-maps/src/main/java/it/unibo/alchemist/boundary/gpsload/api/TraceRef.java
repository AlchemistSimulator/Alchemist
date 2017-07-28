package it.unibo.alchemist.boundary.gpsload.api;

import java.util.Optional;

/**
 * 
 */
public class TraceRef {

    private final String pathFile;
    private final Optional<String> traceName;

    /**
     * @param pathFile
     *            path of the file with the track
     * @param traceName
     *            name of the track if present or Optional.empty
     */
    public TraceRef(final String pathFile, final Optional<String> traceName) {
        this.pathFile = pathFile;
        this.traceName = traceName;
    }

    /**
     * @param pathFile
     *            path of the file with the track
     */
    public TraceRef(final String pathFile) {
        this(pathFile, Optional.empty());
    }

    /**
     * 
     * @return path of the file with the track
     */
    public String getPathFile() {
        return this.pathFile;
    }
    /**
     * 
     * @return name of the track if present or Optional.empty
     */
    public Optional<String> getTraceName() {
        return this.traceName;
    }
}
