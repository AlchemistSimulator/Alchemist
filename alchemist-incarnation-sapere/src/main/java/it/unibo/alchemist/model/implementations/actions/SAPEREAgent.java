/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 */
public abstract class SAPEREAgent extends LsaAbstractAction {

    private static final long serialVersionUID = -471112895569621229L;

    /**
     * Creates a new SAPERE Agent stub. If you use this constructor, you must be
     * sure that your agent does not modify any molecule (e.g. an agent that
     * just moves a node).
     * 
     * @param node
     *            The node in which this agent stays
     */
    public SAPEREAgent(final ILsaNode node) {
        super(node, new ArrayList<ILsaMolecule>(0));
    }

    /**
     * Creates a new SAPERE Agent stub. If you use this constructor, you must be
     * sure that your agent only modifies molecules matching the template passed as m1.
     * 
     * @param node
     *            The node in which this agent stays
     * @param m1
     *            The molecule template it modifies
     */
    public SAPEREAgent(final ILsaNode node, final ILsaMolecule m1) {
        super(node, Arrays.asList(new ILsaMolecule[] { m1 }));
    }

    /**
     * Creates a new SAPERE Agent stub. If you use this constructor, you must be
     * sure that your agent only modifies molecules matching the template passed
     * as m1 and/or the template passed in m2.
     * 
     * @param node
     *            The node in which this agent stays
     * @param m1
     *            The first molecule template it modifies
     * @param m2
     *            The second molecule template it modifies
     */
    public SAPEREAgent(final ILsaNode node, final ILsaMolecule m1, final ILsaMolecule m2) {
        super(node, Arrays.asList(new ILsaMolecule[] { m1, m2 }));
    }

    /**
     * Creates a new SAPERE Agent stub. If you use this constructor, you must be
     * sure that your agent only modifies molecules matching the template passed
     * as m1 and/or the template passed in m2 and/or the template passed in m3.
     * 
     * @param node
     *            The node in which this agent stays
     * @param m1
     *            The first molecule template it modifies
     * @param m2
     *            The second molecule template it modifies
     * @param m3
     *            The third molecule template it modifies
     */
    public SAPEREAgent(final ILsaNode node, final ILsaMolecule m1, final ILsaMolecule m2, final ILsaMolecule m3) {
        super(node, Arrays.asList(new ILsaMolecule[] { m1, m2, m3 }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SAPEREAgent cloneAction(
            final Node<List<ILsaMolecule>> node,
            final Reaction<List<ILsaMolecule>> reaction
    ) {
        throw new UnsupportedOperationException(
                "SAPERE Agents are not meant to be cloned. If you want, implement cloneOnNewNode yourself."
        );
    }

}
