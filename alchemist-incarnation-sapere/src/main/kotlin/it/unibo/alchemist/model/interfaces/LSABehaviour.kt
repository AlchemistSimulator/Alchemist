/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

/**
 * A node's LSA Behaviour.
 */
interface LSABehaviour : Capability<ILsaMolecule> {

    /**
     * Tests whether a node contains a [Molecule].
     *
     * @param mol
     * the molecule to check
     * @return true if the molecule is present, false otherwise
     */
    fun contains(molecule: Molecule): Boolean

    /**
     * @return the count of different molecules in this node
     */
    val moleculeCount: Int

    /**
     * Calculates the concentration of a molecule.
     *
     * @param mol
     * the molecule whose concentration will be returned
     * @return the concentration of the molecule
     */
    fun getConcentration(molecule: Molecule): List<ILsaMolecule>

    /**
     * @return the molecule corresponding to the i-th position
     */
    fun getContents(): Map<Molecule, List<ILsaMolecule>>

    /**
     * Adds an instance of ILsaMolecule in the node's LsaSpace.
     *
     * @param inst
     *            the molecule you want to add
     */
    fun setConcentration(instance: ILsaMolecule)

    /**
     * Deletes an ILsaMolecule from the Node LsaSpace Warning: the method
     * removes only the first matched ILsaMolecule.
     *
     * @param matchedInstance
     *            the molecule you want to remove
     * @return true if the remove operation finish correctly, false otherwise.
     *
     */
    fun removeConcentration(matchedInstance: ILsaMolecule): Boolean

    /**
     * @return lsaMolecules in the node. This backs the internal LsaSpace: USE READ ONLY.
     */
    fun getLsaSpace(): List<ILsaMolecule>
}
