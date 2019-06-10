/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview.model;

import java.util.Map;

/**
 * An entity which is able to produce a structure for Batch mode section of Alchemist project.
 *
 */
public class Batch {

    private boolean select;
    private Map<String, Boolean> variables;
    private int thread;

    /**
     * 
     * @return a number of threads to use.
     */
    public int getThreadCount() {
        return this.thread;
    }

    /**
     * 
     * @return a map of variables with value true if it is selected.
     */
    public Map<String, Boolean> getVariables() {
        return this.variables;
    }

    /**
     * 
     * @return true if the Batch mode section switch is selected, otherwise false.
     */
    public boolean isSelected() {
        return this.select;
    }

    /**
     * 
     * @param thread a number of threads.
     */
    public void setThreadCount(final int thread) {
        this.thread = thread;
    }

    /**
     * 
     * @param var a map of variables.
     */
    public void setVariables(final Map<String, Boolean> var) {
        this.variables = var;
    }

    /**
     * 
     * @param sel true if the Batch mode section switch must be selected, otherwise false.
     */
    public void setSelected(final boolean sel) {
        this.select = sel;
    }

}
