package it.unibo.alchemist.boundary.projectview.model;

import java.util.List;

/**
 * 
 *
 */
public class BatchImpl implements Batch {

    private boolean select;
    private List<String> variables;
    private int thread;

    @Override
    public int getThread() {
        return this.thread;
    }

    @Override
    public List<String> getVariables() {
        return this.variables;
    }

    @Override
    public boolean isSelect() {
        return this.select;
    }

    @Override
    public void setThread(final int thread) {
        this.thread = thread;
    }

    @Override
    public void setVariables(final List<String> var) {
        this.variables = var;
    }

    @Override
    public void setSelect(final boolean sel) {
        this.select = sel;
    }

}
