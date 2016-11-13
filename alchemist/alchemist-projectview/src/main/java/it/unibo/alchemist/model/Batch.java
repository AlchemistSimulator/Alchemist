package it.unibo.alchemist.model;

import java.util.List;

/**
 * 
 *
 */
public class Batch implements IBatch {

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
