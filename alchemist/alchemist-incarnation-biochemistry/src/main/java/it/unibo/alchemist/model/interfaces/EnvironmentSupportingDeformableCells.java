package it.unibo.alchemist.model.interfaces;

/**
 * {@link Environment} supporting deformable cells.
 */
public interface EnvironmentSupportingDeformableCells<P extends Position<? extends P>> extends Environment<Double, P> {

    /**
     * 
     * @return the biggest among the deformable cell's diameter, when not stressed. 
     */
    double getMaxDiameterAmongCircularDeformableCells();
}
