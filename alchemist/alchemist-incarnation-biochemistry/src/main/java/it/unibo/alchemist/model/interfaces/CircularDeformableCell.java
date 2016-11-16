package it.unibo.alchemist.model.interfaces;

/**
 * Implements a circular deformable cell.
 *
 */
public interface CircularDeformableCell extends CellWithCircularArea {

    /**
     * 
     * @return the max diameter that this cell can have, e.g. the diameter that this cell has if no other cell is around.
     */
    double getMaxDiameter();

    /**
     * 
     * @return the max radius that this cell can have, e.g. the radius that this cell has if no other cell is around.
     */
    double getMaxRadius();
}
