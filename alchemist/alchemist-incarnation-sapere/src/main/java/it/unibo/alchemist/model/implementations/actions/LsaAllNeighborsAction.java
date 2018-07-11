/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;

import java.util.List;



/**
 * The execution involves all neighbours. The molecule can be different from a
 * neighbor to the other, because some special Variable (#D) are specific for
 * each node. This is why N molecules are instanced if there are N neighbours.
 * 
 */
public class LsaAllNeighborsAction extends LsaRandomNeighborAction {

    private static final long serialVersionUID = -4798752202640197182L;

    /**
     * @param node
     *            the node in which the reaction is programmed
     * @param molecule
     *            the lsa which is involved
     * @param env
     *            the current environment
     */
    public LsaAllNeighborsAction(final ILsaNode node, final ILsaMolecule molecule, final Environment<List<ILsaMolecule>, ?> env) {
        this(node, molecule, env, null);
    }

    /**
     * @param node
     *            the node in which the reaction is programmed
     * @param molecule
     *            the lsa which is involved
     * @param env
     *            the current environment
     * @param r
     *            unused. Can be null.
     */
    public LsaAllNeighborsAction(final ILsaNode node, final ILsaMolecule molecule, final Environment<List<ILsaMolecule>, ?> env, final RandomGenerator r) {
        super(node, molecule, env, r);
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
