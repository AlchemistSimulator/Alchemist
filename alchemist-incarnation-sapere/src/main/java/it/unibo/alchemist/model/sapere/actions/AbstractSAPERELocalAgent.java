/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;

import java.io.Serial;

/**
 * A SAPERE Agent that does not do actions on neighbors.
 *
 */
public abstract class AbstractSAPERELocalAgent extends AbstractSAPEREAgent {

    @Serial
    private static final long serialVersionUID = 8720614570156227036L;

    /**
     * Creates a new SAPERE Local Agent stub. If you use this constructor, you must be
     * sure that your agent does not modify any molecule (e.g., an agent that
     * just moves a node).
     *
     * @param node
     *            The node in which this agent stays
     */
    public AbstractSAPERELocalAgent(final ILsaNode node) {
        super(node);
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
    public AbstractSAPERELocalAgent(final ILsaNode node, final ILsaMolecule m1) {
        super(node, m1);
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
    public AbstractSAPERELocalAgent(final ILsaNode node, final ILsaMolecule m1, final ILsaMolecule m2) {
        super(node, m1, m2);
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
    public AbstractSAPERELocalAgent(final ILsaNode node, final ILsaMolecule m1, final ILsaMolecule m2, final ILsaMolecule m3) {
        super(node, m1, m2, m3);
    }

    @Override
    public final Context getContext() {
        return Context.LOCAL;
    }

}
