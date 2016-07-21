package it.unibo.alchemist.model.interfaces;

/**
 * Implements a cell with a defined volume.
 *
 */
public interface CellWithCircularArea extends CellNode {

    /**
     * @return the cell's diameter.
     */
    double getDiameter();

    /**
     * @return the cell's radius.
     */
    double getRadius();

}
