package it.unibo.alchemist.model;

import java.util.List;

/**
 * 
 *
 */
public class Project implements IProject {

    private String simulation;
    private int endTime;
    private String effect;
    private Output output;
    private Batch batch;
    private List<String> classpath;

    @Override
    public Batch getBatch() {
        return this.batch;
    }

    @Override
    public List<String> getClasspath() {
        return this.classpath;
    }

    @Override
    public int getEndTime() {
        return this.endTime;
    }

    @Override
    public String getEffect() {
        return this.effect;
    }

    @Override
    public Output getOutput() {
        return this.output;
    }

    @Override
    public String getSimulation() {
        return this.simulation;
    }

    @Override
    public void setBatch(final Batch batch) {
        this.batch = batch;
    }

    @Override
    public void setClasspath(final List<String> classpath) {
        this.classpath = classpath;
    }

    @Override
    public void setEndTime(final int endTime) {
        this.endTime = endTime;
    }

    @Override
    public void setEffect(final String eff) {
        this.effect = eff;
    }

    @Override
    public void setOutput(final Output out) {
        this.output = out;
    }

    @Override
    public void setSimulation(final String sim) {
        this.simulation = sim;
    }

}
