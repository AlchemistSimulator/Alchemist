package it.unibo.alchemist.model;

/**
 * An entity which is able to produce a structure for Output section of Alchemist project.
 *
 */
public interface IOutput {

    /**
     * 
     * @return a base name of output file.
     */
    String getBaseName();

    /**
     * 
     * @return a path of output folder.
     */
    String getFolder();

    /**
     * 
     * @return a sampling interval.
     */
    double getSamplInterval();

    /**
     * 
     * @return true if the switch of Output section is selected, otherwise false.
     */
    boolean isSelect();

    /**
     * 
     * @param baseName a base name for output file.
     */
    void setBaseName(final String baseName);

    /**
     * 
     * @param folder a path for output folder.
     */
    void setFolder(final String folder);

    /**
     * 
     * @param samplInterval a sampling interval.
     */
    void setSamplInterval(final double samplInterval);

    /**
     * 
     * @param sel true if the switch of Output section must be selected, otherwise false.
     */
    void setSelect(final boolean sel);

}
