package it.unibo.alchemist.model.implementations.cellshapes;

import it.unibo.alchemist.model.interfaces.CellShape;

/**
 * Implements a circolar shape for a cell.
 */
public class CircolarShape implements CellShape {

    private static final double STANDARD_DIAMETER = 10;

    private final double diameter;

    /**
     * Initialize a new {@link CircolarShape} with diameter 10.
     */
    public CircolarShape() {
        diameter = STANDARD_DIAMETER;
    }

    /**
     * Initialize a new {@link CircolarShape} with the given diameter.
     * @param diam 
     */
    public CircolarShape(final double diam) {
        diameter = diam;
    }

    @Override
    public double getMaxRange() {
        return diameter;
    }

}
