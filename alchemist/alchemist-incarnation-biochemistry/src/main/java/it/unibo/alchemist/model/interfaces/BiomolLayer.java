package it.unibo.alchemist.model.interfaces;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;

/**
 * A specific interface of Layers in incarnation biochemistry.
 *
 */
public interface BiomolLayer extends Layer<Double> {

    /**
     * 
     * @return the {@link Biomolecule} contained in this {@link BiomolLayer}.
     */
    Biomolecule getBiomolecule();

    /**
     * 
     * @param biomol a {@link Biomolecule}.
     * @return true if biomol is present in this {@link BiomolLayer}.
     */
    boolean isBiomoleculePresent(final Biomolecule biomol); 

}
