package it.unibo.alchemist.boundary.projectview.model;

/**
 * 
 *
 */
public class OutputImpl implements Output {

    private boolean select;
    private String folder;
    private String baseName;
    private double samplInterval;

    @Override
    public String getBaseName() {
        return this.baseName;
    }

    @Override
    public String getFolder() {
        return this.folder;
    }

    @Override
    public double getSamplInterval() {
        return this.samplInterval;
    }

    @Override
    public boolean isSelect() {
        return this.select;
    }

    @Override
    public void setBaseName(final String baseName) {
        this.baseName = baseName;
    }

    @Override
    public void setFolder(final String folder) {
        this.folder = folder;
    }

    @Override
    public void setSamplInterval(final double samplInterval) {
        this.samplInterval = samplInterval;
    }

    @Override
    public void setSelect(final boolean sel) {
        this.select = sel;
    }

}
