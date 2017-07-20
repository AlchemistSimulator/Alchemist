package it.unibo.alchemist.boundary.gpsload;

/**
 * 
 */
public class MappingTrace {

    private static final String DEFAULT_TRACE_NAME = null;
    private final int idNode;
    private final String pathFile;
    private final String traceName;
    /**
     * 
     * @param idNode
     * @param gpxPathFile
     * @param gpxTraceName
     */
    public MappingTrace(final int idNode, final String pathFile, final String traceName) {
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
        this(idNode, gpxPathFile, DEFAULT_TRACE_NAME);
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
    public String getTraceName() {
        return this.traceName;
    }
}
