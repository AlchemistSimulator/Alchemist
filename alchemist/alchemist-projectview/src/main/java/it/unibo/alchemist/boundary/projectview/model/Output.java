package it.unibo.alchemist.boundary.projectview.model;

/**
 * An entity which is able to produce a structure for Output section of Alchemist project.
 *
 */
public class Output {

    private boolean select;
    private String folder;
    private String baseName;
    private double samplInterval;

    /**
     * 
     * @return a base name of output file.
     */
    public String getBaseName() {
        return this.baseName;
    }

    /**
     * 
     * @return a path of output folder.
     */
    public String getFolder() {
        return this.folder;
    }

    /**
     * 
     * @return a sampling interval.
     */
    public double getSampleInterval() {
        return this.samplInterval;
    }

    /**
     * 
     * @return true if the switch of Output section is selected, otherwise false.
     */
    public boolean isSelected() {
        return this.select;
    }

    /**
     * 
     * @param baseName a base name for output file.
     */
    public void setBaseName(final String baseName) {
        this.baseName = baseName;
    }

    /**
     * 
     * @param folder a path for output folder.
     */
    public void setFolder(final String folder) {
        this.folder = folder;
    }

    /**
     * 
     * @param samplInterval a sampling interval.
     */
    public void setSampleInterval(final double samplInterval) {
        this.samplInterval = samplInterval;
    }

    /**
     * 
     * @param sel true if the switch of Output section must be selected, otherwise false.
     */
    public void setSelected(final boolean sel) {
        this.select = sel;
    }

}
