/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serial;
import java.util.List;

/**
 * The execution involves all neighbors.
 * The molecule can be different from a
 * neighbor to the other because some special Variable (#D) is specific for
 * each node.
 * This is why N molecules are instanced if there are N neighbors.
 */
public final class LsaAllNeighborsAction extends LsaRandomNeighborAction {

    @Serial
    private static final long serialVersionUID = -4798752202640197182L;

    /**
     * @param node
     *            the node in which the reaction is programmed
     * @param molecule
     *            the LSA
     * @param environment
     *            the current environment
     */
    public LsaAllNeighborsAction(
            final ILsaNode node,
            final ILsaMolecule molecule,
            final Environment<List<ILsaMolecule>, ?> environment
    ) {
        this(node, molecule, environment, null);
    }

    /**
     * @param node
     *            the node in which the reaction is programmed
     * @param molecule
     *            the LSA
     * @param environment
     *            the current environment
     * @param randomGenerator
     *            unused. Can be null.
     */
    public LsaAllNeighborsAction(
            final ILsaNode node,
            final ILsaMolecule molecule,
            final Environment<List<ILsaMolecule>, ?> environment,
            final RandomGenerator randomGenerator
    ) {
        super(node, molecule, environment, randomGenerator);
    }

    @Override
    public void execute() {
        for (final ILsaNode node : getNodes()) {
            setSynthectics(node);
            setConcentration(node);
        }
    }

    @Override
    public String toString() {
        return "*" + getMolecule().toString();
    }

}
