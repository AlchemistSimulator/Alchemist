package it.unibo.alchemist.model;

import java.util.List;

/**
 * An entity which is able to produce a structure for an Alchemist project to go through a Json reader or writer.
 *
 */
public interface IProject {

    /**
     * 
     * @return an entity of Batch mode.
     */
    Batch getBatch();

    /**
     * 
     * @return a list of the libraries to add to the classpath.
     */
    List<String> getClasspath();

    /**
     * 
     * @return a end time of simulation.
     */
    int getEndTime();

    /**
     * 
     * @return a path of effect file.
     */
    String getEffect();

    /**
     * 
     * @return an entity of the Output.
     */
    Output getOutput();

    /**
     * 
     * @return a path of simulation file.
     */
    String getSimulation();

    /**
     * 
     * @param batch a entity of Batch mode.
     */
    void setBatch(final Batch batch);

    /**
     * 
     * @param classpath a list of libraries.
     */
    void setClasspath(final List<String> classpath);

    /**
     * 
     * @param endTime an end time.
     */
    void setEndTime(final int endTime);

    /**
     * 
     * @param eff a path of a effect file.
     */
    void setEffect(final String eff);

    /**
     * 
     * @param out an entity of Output.
     */
    void setOutput(final Output out);

    /**
     * 
     * @param sim a path of a simulation file.
     */
    void setSimulation(final String sim);
}
