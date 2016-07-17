package it.unibo.alchemist.model.interfaces;

/**
 * Implements a cell with a defined volume.
 *
 */
public interface ICellNodeWithShape extends ICellNode{
    
    /**
     * @return the cell's volume.
     */
    public CellShape getShape();
    

}
