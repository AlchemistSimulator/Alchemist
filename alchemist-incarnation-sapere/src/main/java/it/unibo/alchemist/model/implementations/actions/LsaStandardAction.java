/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.expressions.implementations.ConstTreeNode;
import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.lang.HashString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static it.unibo.alchemist.model.implementations.molecules.LsaMolecule.SYN_NODE_ID;
import static it.unibo.alchemist.model.implementations.molecules.LsaMolecule.SYN_RAND;


/**
 * This action add LsaMolecule in a single node.
 * 
 * 
 */
public class LsaStandardAction extends LsaAbstractAction {

    private static final long serialVersionUID = -7034948679002996913L;
    private static final Logger L = LoggerFactory.getLogger(LsaStandardAction.class);
    private final boolean initRand, initNode;
    private final ILsaMolecule mol;
    private final ITreeNode<?> nodeId;
    @SuppressFBWarnings(
            value = "SE_BAD_FIELD",
            justification = "All provided RandomGenerator implementations are actually Serializable"
    )
    private final RandomGenerator rand;

    /**
     * Builds a new local action, withouth any RandomGenerator. #RANDOM.
     * 
     * @param n
     *            The source node
     * @param m
     *            The ILsaMolecule instance you want to add to the node lsa
     *            space.
     */
    public LsaStandardAction(final ILsaMolecule m, final ILsaNode n) {
        this(m, n, null);
    }

    /**
     * Builds a new local action.
     * 
     * @param n
     *            The source node
     * @param m
     *            The ILsaMolecule instance you want to add to the node lsa
     *            space.
     * @param random
     *            The Random generator to use
     */
    public LsaStandardAction(final ILsaMolecule m, final ILsaNode n, final RandomGenerator random) {
        super(n, Collections.singletonList(m));
        mol = Objects.requireNonNull(m);
        rand = random;
        final String molString = mol.toString();
        initRand = molString.contains(SYN_RAND);
        initNode = molString.contains(SYN_NODE_ID);
        if (initRand && random == null) {
            L.warn(SYN_RAND + " is used in " + m
                    + ", but the RandomGenerator has not been initialized. This WILL lead to problems.");
        }
        nodeId = initNode ? new ConstTreeNode(new HashString("node" + n.getId())) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LsaStandardAction cloneAction(final Node<List<ILsaMolecule>> node, final Reaction<List<ILsaMolecule>> reaction) {
        return new LsaStandardAction(getMolecule(), (ILsaNode) node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        setConcentration(getNode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    /**
     * @return the modified molecule
     */
    public ILsaMolecule getMolecule() {
        return mol;
    }

    /**
     * Executes on the passed node.
     * 
     * @param node
     *            the node where to execute
     */
    protected void setConcentration(final ILsaNode node) {
        if (initRand) {
            addMatch(SYN_RAND, new NumTreeNode(rand.nextDouble()));
        }
        if (initNode) {
            addMatch(SYN_NODE_ID, nodeId);
        }
        final List<IExpression> l = getMolecule().allocateVar(getMatches());
        node.setConcentration(new LsaMolecule(l));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getMolecule().toString();
    }

}
