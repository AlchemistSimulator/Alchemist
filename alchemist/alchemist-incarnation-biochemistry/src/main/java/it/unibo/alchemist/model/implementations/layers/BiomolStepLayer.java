package it.unibo.alchemist.model.implementations.layers;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.BiomolLayer;

public class BiomolStepLayer extends StepLayer<Double> implements BiomolLayer {

    private final Biomolecule biomolecule;

    public BiomolStepLayer(
            final double mx, 
            final double my, 
            final Double minValue, 
            final Double maxValue, 
            final Biomolecule biomol) {
        super(mx, my, minValue, maxValue);
        biomolecule = biomol;
    }

    public BiomolStepLayer(final Double minValue, final Double maxValue, final Biomolecule biomol) {
        this(0, 0, minValue, maxValue, biomol);
    }

    @Override
    public Biomolecule getBiomolecule() {
        return biomolecule;
    }

    @Override
    public boolean isBiomoleculePresent(final Biomolecule biomol) {
        return biomolecule.equals(biomol);
    }

}
