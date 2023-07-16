/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere;

import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;

import java.util.List;

/**
 * Interface for an LSA Node. Avoids the crappy casts.
 * 
 */
public interface ILsaNode extends Node<List<ILsaMolecule>> {

    /**
     * Adds an instance of ILsaMolecule in the node's LsaSpace.
     * 
     * @param inst
     *            the molecule you want to add
     */
    void setConcentration(ILsaMolecule inst);

    /**
     * Deletes an ILsaMolecule from the Node LsaSpace Warning: the method
     * removes only the first matched ILsaMolecule.
     * 
     * @param matchedInstance
     *            the molecule you want to remove
     * @return true if the remove operation finish correctly, false otherwise.
     * 
     */
    boolean removeConcentration(ILsaMolecule matchedInstance);

    /**
     * @return lsaMolecules in the node. This backs the internal LsaSpace: USE READ ONLY.
     */
    List<ILsaMolecule> getLsaSpace();

    @Override
    List<ILsaMolecule> getConcentration(Molecule mol);

}
