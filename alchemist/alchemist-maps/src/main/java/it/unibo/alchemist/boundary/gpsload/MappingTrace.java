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
     * @param idNode id of the node
     * @param pathFile path of the file with the track
     * @param traceName name of the track if present or Optional.empty
     */
    public MappingTrace(final int idNode, final String pathFile, final Optional<String> traceName) {
        this.idNode = idNode;
        this.pathFile = pathFile;
        this.traceName = traceName;
    }
    /**
     * 
     * @param idNode id of the node
     * @param pathFile path of the file with the track
     */
    public MappingTrace(final int idNode, final String pathFile) {
        this(idNode, pathFile, Optional.empty());
    }
    /**
     * 
     * @return the id of the node
     */
    public int getIdNode() {
        return this.idNode;
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
