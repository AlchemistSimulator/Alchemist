/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

/**
 * Represents a specific point on the Earth's surface.
 */
public interface GeoPosition extends Position2D<GeoPosition> {

    /**
     * @return the latitude
     */
    double getLatitude();

    /**
     * @return the longitude
     */
    double getLongitude();

    @Deprecated
    @Override
    double getCoordinate(int dimension);

    /**
     * Adds two {@link GeoPosition}.
     *
     * @param other the position to sum to
     * @return the sum.
     */
    GeoPosition plus(GeoPosition other);

    /**
     * Subtracts the provided {@link GeoPosition} from this {@link GeoPosition}.
     *
     * @param other the position to subtract to this one
     * @return the difference.
     */
    GeoPosition minus(GeoPosition other);
}
