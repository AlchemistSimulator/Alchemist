/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * 
 */
package it.unibo.alchemist.core.interfaces;

/**
 * 
 *          This enum represents the possible states in which a Simulation could
 *          be.
 * 
 */

//please note that changing the order of the elements may broke the compareTo method in Simulation

public enum Status {

    /**
     * The simulation is being initialized.
     */
    INIT,
    /**
     * The simulation is ready to be started.
     */
    READY,
    /**
     * The simulation is paused. It can be resumed.
     */
    PAUSED,
    /**
     * The simulation is currently running.
     */
    RUNNING,
    /**
     * The simulation is stopped. It is no longer possible to resume
     * it.
     */
    TERMINATED;

}
