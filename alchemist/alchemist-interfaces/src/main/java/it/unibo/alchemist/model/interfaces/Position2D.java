package it.unibo.alchemist.model.interfaces;

public interface Position2D<P extends Position2D<? extends P>> extends Position<P> {
    
    double getX();
    
    double getY();

}
