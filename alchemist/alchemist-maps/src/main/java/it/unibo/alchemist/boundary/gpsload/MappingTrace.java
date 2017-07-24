package it.unibo.alchemist.boundary.gpsload;

import java.util.Optional;

/**
 * 
 */
public class MappingTrace {

    private final int idNode;
    private final String pathFile;
    private final Optional<String> traceName;
    /**
     * 
     * @param idNode
     * @param gpxPathFile
     * @param gpxTraceName
     */
    public MappingTrace(final int idNode, final String pathFile, final Optional<String> traceName) {
        this.idNode = idNode;
        this.pathFile = pathFile;
        this.traceName = traceName;
    }
    /**
     * 
     * @param idNode
     * @param gpxPathFile
     */
    public MappingTrace(final int idNode, final String gpxPathFile) {
        this(idNode, gpxPathFile, Optional.empty());
    }
    /**
     * 
     * @return
     */
    public int getIdNode() {
        return this.idNode;
    }
    /**
     * 
     * @return
     */
    public String getPathFile() {
        return this.pathFile;
    }
    /**
     * 
     * @return
     */
    public Optional<String> getTraceName() {
        return this.traceName;
    }
}
