package it.unibo.alchemist.model.interfaces;

/**
 * Represents a specific point on the Earth's surface.
 */
public interface GeoPosition extends Position {

    /**
     * @return the latitude
     */
    double getLatitude();

    /**
     * @return the longitude
     */
    double getLongitude();

}
