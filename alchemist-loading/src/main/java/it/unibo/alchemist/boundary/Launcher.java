/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary;

/**
 * An entity with a [name] that can take responsibility for performing an Alchemist run.
 */
public interface Launcher<M> {

    /**
     * Launches the simulation.
     * @param loader loader
     */
    default Progress<M> launch(Loader loader) {
        return launch(loader, false);
    }

    /**
     * Launches the simulation.
     * @param loader loader
     */
    Progress<M> launch(Loader loader, boolean forceAutostart);

}
