/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps;

/**
 * An object that follows a {@link GPSTrace}.
 */
public interface ObjectWithGPS {

    /**
     * Sets the GPS trace to follow.
     *
     * @param trace the {@link GPSTrace} to follow
     */
    void setTrace(GPSTrace trace);

}
