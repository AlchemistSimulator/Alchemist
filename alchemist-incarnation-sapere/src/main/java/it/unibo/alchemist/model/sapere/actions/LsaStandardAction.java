/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import it.unibo.alchemist.model.sapere.dsl.impl.ConstTreeNode;
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.lang.HashString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This action adds an LsaMolecule in a single node.
 */
public class LsaStandardAction extends AbstractLsaAction {

    private static final Logger L = LoggerFactory.getLogger(LsaStandardAction.class);
    private final boolean initRand;
    private final boolean initNode;
    private final ILsaMolecule mol;
    private final ITreeNode<?> nodeId;
    private final RandomGenerator rand;

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
    public LsaStandardAction(final RandomGenerator random, final ILsaNode n, final ILsaMolecule m) {
        super(n, Collections.singletonList(m));
        mol = Objects.requireNonNull(m);
        rand = random;
        final String molString = mol.toString();
        initRand = molString.contains(LsaMolecule.SYN_RAND);
        initNode = molString.contains(LsaMolecule.SYN_NODE_ID);
        if (initRand && random == null) {
            L.warn(
                "{} is used in {}, but the RandomGenerator has not been initialized. This WILL lead to problems.",
                LsaMolecule.SYN_RAND,
                m
            );
        }
        nodeId = initNode ? new ConstTreeNode(new HashString("node" + n.getId())) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public LsaStandardAction cloneAction(
        @Nonnull final Node<List<ILsaMolecule>> node,
        @Nonnull final NodeReaction<List<ILsaMolecule>> reaction
    ) {
        return new LsaStandardAction(rand, (ILsaNode) node, getMolecule());
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
    @Nonnull
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
            addMatch(LsaMolecule.SYN_RAND, new NumTreeNode(rand.nextDouble()));
        }
        if (initNode) {
            addMatch(LsaMolecule.SYN_NODE_ID, nodeId);
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
