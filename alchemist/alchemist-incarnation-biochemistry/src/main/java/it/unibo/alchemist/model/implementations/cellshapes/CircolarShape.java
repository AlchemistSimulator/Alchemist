package it.unibo.alchemist.model.implementations.cellshapes;

import it.unibo.alchemist.model.interfaces.CellShape;

public class CircolarShape implements CellShape {
    
    private static final double STANDARD_DIAMETER = 10;
    
    private final double diameter;
    
    public CircolarShape() {
        diameter = STANDARD_DIAMETER;
    }
    
    public CircolarShape(double diam) {
        diameter = diam;
    }

    @Override
    public double getMaxRange() {
        return diameter;
    }

}
