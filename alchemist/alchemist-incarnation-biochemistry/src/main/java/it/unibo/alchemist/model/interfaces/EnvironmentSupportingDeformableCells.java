package it.unibo.alchemist.model.interfaces;

/**
 * {@link Environment} supporting deformable cells.
 */
public interface EnvironmentSupportingDeformableCells extends Environment<Double> {

    /**
     * 
     * @return the biggest among the deformable cell's diameter, when not stressed. 
     */
    double getMaxDiameterAmongDeformableCells();
}
