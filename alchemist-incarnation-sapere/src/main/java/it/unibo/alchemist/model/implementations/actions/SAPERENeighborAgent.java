/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Position;

import java.util.List;


/**
 * A SAPERE Agent that modifies something on neighboring nodes.
 *
 * @param <P> position type
 */
public abstract class SAPERENeighborAgent<P extends Position<P>> extends SAPEREAgent {

    private static final long serialVersionUID = 8720614570156227036L;
    private final Environment<List<ILsaMolecule>, P> environment;

    /**
     * Creates a new SAPERE Neighbor Agent stub. If you use this constructor,
     * you must be sure that your agent only modifies molecules matching the
     * template passed as m1.
     * 
     * @param environment
     *            The current environment
     * @param node
     *            The node in which this agent stays
     * @param m1
     *            The molecule template it modifies
     */
    public SAPERENeighborAgent(final Environment<List<ILsaMolecule>, P> environment, final ILsaNode node, final ILsaMolecule m1) {
        super(node, m1);
        this.environment = environment;
    }

    /**
     * Creates a new SAPERE Agent stub. If you use this constructor, you must be
     * sure that your agent only modifies molecules matching the template passed
     * as m1 and/or the template passed in m2.
     * 
     * @param environment
     *            The current environment
     * @param node
     *            The node in which this agent stays
     * @param m1
     *            The first molecule template it modifies
     * @param m2
     *            The second molecule template it modifies
     */
    public SAPERENeighborAgent(
            final Environment<List<ILsaMolecule>, P> environment,
            final ILsaNode node,
            final ILsaMolecule m1,
            final ILsaMolecule m2
    ) {
        super(node, m1, m2);
        this.environment = environment;
    }

    /**
     * Creates a new SAPERE Agent stub. If you use this constructor, you must be
     * sure that your agent only modifies molecules matching the template passed
     * as m1 and/or the template passed in m2 and/or the template passed in m3.
     * 
     * @param environment
     *            The current environment
     * @param node
     *            The node in which this agent stays
     * @param m1
     *            The first molecule template it modifies
     * @param m2
     *            The second molecule template it modifies
     * @param m3
     *            The third molecule template it modifies
     */
    public SAPERENeighborAgent(
            final Environment<List<ILsaMolecule>, P> environment,
            final ILsaNode node, final ILsaMolecule m1,
            final ILsaMolecule m2,
            final ILsaMolecule m3
    ) {
        super(node, m1, m2, m3);
        this.environment = environment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.model.interfaces.Action#getContext()
     */
    @Override
    public final Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    /**
     * @return the current position of the node
     */
    protected final P getCurrentPosition() {
        return environment.getPosition(getNode());
    }

    /**
     * @param node
     *            the node
     * @return the position of node
     */
    protected final P getPosition(final ILsaNode node) {
        return environment.getPosition(node);
    }

    /**
     * @param node
     *            the node
     * @return the position of node
     */
    protected final Neighborhood<List<ILsaMolecule>> getNeighborhood(final ILsaNode node) {
        return environment.getNeighborhood(node);
    }

    /**
     * @return the position of node
     */
    protected final Neighborhood<List<ILsaMolecule>> getLocalNeighborhood() {
        return environment.getNeighborhood(getNode());
    }

    /**
     * @param direction the point towards which move the node
     */
    protected final void move(final P direction) {
        environment.moveNodeToPosition(getNode(), getEnvironment().getPosition(getNode()).plus(direction.getCoordinates()));
    }

    /**
     * @param mol the molecule template to allocate and inject
     * @param n the node where to inject the template
     */
    protected void allocateAndInject(final ILsaMolecule mol, final ILsaNode n) {
        n.setConcentration(new LsaMolecule(mol.allocateVar(getMatches())));
    }

    /**
     * @return the current environment
     */
    protected Environment<List<ILsaMolecule>, P> getEnvironment() {
        return environment;
    }

}
