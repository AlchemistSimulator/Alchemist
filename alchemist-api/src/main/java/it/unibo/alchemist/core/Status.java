/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core;

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

    /**
     *
     * @param s the destination status
     * @return true if the provided status can be reached from this status (i.e., if the simulation lifecycle allows to
     * get from the current status to the provided one).
     */
    public boolean isReachableFrom(final Status s) {
        return compareTo(s) >= 0 || this == PAUSED && s == RUNNING;
    }

}
