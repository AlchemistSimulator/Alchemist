package it.unibo.alchemist.boundary.projectview.model;

import java.util.List;

/**
 * An entity which is able to produce a structure for Batch mode section of Alchemist project.
 *
 */
public interface Batch {

    /**
     * 
     * @return a number of threads to use.
     */
    int getThread();

    /**
     * 
     * @return a list of selected variables.
     */
    List<String> getVariables();

    /**
     * 
     * @return true if the Batch mode section switch is selected, otherwise false.
     */
    boolean isSelect();

    /**
     * 
     * @param thread a number of threads.
     */
    void setThread(final int thread);

    /**
     * 
     * @param var a list of variables.
     */
    void setVariables(final List<String> var);

    /**
     * 
     * @param sel true if the Batch mode section switch must be selected, otherwise false.
     */
    void setSelect(final boolean sel);

}
