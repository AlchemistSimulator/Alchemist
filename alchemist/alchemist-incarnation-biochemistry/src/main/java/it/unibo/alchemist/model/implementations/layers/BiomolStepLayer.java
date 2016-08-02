package it.unibo.alchemist.model.implementations.layers;

public class BiomolStepLayer extends StepLayer<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = -8851470313973045006L;

    public BiomolStepLayer(
            final double mx, 
            final double my, 
            final Double maxValue, 
            final Double minValue) {
        super(mx, my, maxValue, minValue);
    }

    public BiomolStepLayer(final Double maxValue, final Double minValue) {
        this(0, 0, maxValue, minValue);
    }

}
