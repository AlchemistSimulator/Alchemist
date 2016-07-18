package it.unibo.alchemist.model.interfaces;

/**
 * Interface for all possible cell's shapes. 
 *
 */
public interface CellShape {
    /**
     *  @return the maximum range within no cell has to be found to correctly place the cell with this shape.
     */
    double getMaxRange();
}
