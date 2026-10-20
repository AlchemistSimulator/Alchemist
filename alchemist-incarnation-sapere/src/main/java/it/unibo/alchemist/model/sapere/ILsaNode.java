/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere;

import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.observation.Observable;
import it.unibo.alchemist.model.observation.ObservableList;

import java.util.List;

/**
 * Interface for an LSA Node.
 */
public interface ILsaNode extends Node<List<ILsaMolecule>> {

    /**
     * Adds an instance of ILsaMolecule in the node's LsaSpace.
     *
     * @param inst
     *            the molecule you want to add
     */
    void addLsa(ILsaMolecule inst);

    /**
     * Returns the LSA molecules in this node.
     *
     * @return lsaMolecules in the node. This backs the internal LsaSpace: USE READ-ONLY.
     */
    List<ILsaMolecule> getLsaSpace();

    /**
     * Finds the LSA instances in this node that [template] matches.
     *
     * @param template the LSA template
     * @return the matched instances, empty if [template] matches none
     */
    List<ILsaMolecule> matching(ILsaMolecule template);

    /**
     * Observes the instances that [template] may match: those sharing its first argument when it is a constant
     * or a number, every instance otherwise. Matching them against [template] is up to the caller.
     *
     * @param template the LSA template
     * @return an observable that emits the instances that [template] may match
     */
    Observable<List<ILsaMolecule>> observeCandidates(ILsaMolecule template);

    /**
     * Observes the LSA space of this node.
     *
     * @return an {@link ObservableList} view of LSA Molecules in the node.
     */
    ObservableList<ILsaMolecule> observeLsaSpace();

    /**
     * Deletes an ILsaMolecule from the Node LsaSpace Warning: the method
     * removes only the first matched ILsaMolecule.
     *
     * @param matchedInstance
     *            the molecule you want to remove
     * @return true if the remove operation finishes correctly, false otherwise.
     *
     */
    boolean removeOneMatchedLsa(ILsaMolecule matchedInstance);

}
