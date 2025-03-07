/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.lang.HashString;

import java.io.Serial;
import java.util.List;

/**
 */
public final class LsaCountNeighborsAction extends AbstractSAPERELocalAgent {

    @Serial
    private static final long serialVersionUID = -7128058274012426458L;
    private final HashString countVarName;
    private final Environment<List<ILsaMolecule>, ?> environment;
    private final ILsaMolecule mol;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All implementations are actually serializable")
    private final RandomGenerator rnd;

    /**
     * Builds a new action that counts neighbors which contain in their lsaSpace
     * an lsaMolecule matching mol. The effect of this Action is to add to the
     * matches list the variable countVar. The execution has no effect on the
     * set of influenced molecules for the reaction.
     *
     * @param environment
     *            The environment to use
     * @param node
     *            The source node
     * @param molToCount
     *            The IlsaMolecule instance you want to search in neighbor lsa
     *            space.
     * @param countVar
     *            The String representing the name of the counting var. (to add
     *            to a matches map)
     * @param rand
     *            Random engine
     */
    public LsaCountNeighborsAction(
            final Environment<List<ILsaMolecule>, ?> environment,
            final ILsaNode node,
            final ILsaMolecule molToCount,
            final HashString countVar,
            final RandomGenerator rand
    ) {
        super(node);
        rnd = rand;
        this.environment = environment;
        countVarName = new HashString(countVar);
        mol = molToCount;
    }

    /**
     * Builds a new action that counts neighbors which contain in their lsaSpace
     * an lsaMolecule matching mol. The effect of this Action is to add to the
     * matches list the variable countVar. The execution has no effect on the
     * set of influenced molecules for the reaction.
     *
     * @param environment
     *            The environment to use
     * @param node
     *            The source node
     * @param molToCount
     *            The IlsaMolecule instance you want to search in neighbor lsa
     *            space.
     * @param countVar
     *            The String representing the name of the counting var. (to add
     *            to the matches map)
     * @param rand
     *            Random engine
     */
    public LsaCountNeighborsAction(
            final Environment<List<ILsaMolecule>, ?> environment,
            final ILsaNode node,
            final ILsaMolecule molToCount,
            final String countVar,
            final RandomGenerator rand
    ) {
        this(environment, node, molToCount, new HashString(countVar), rand);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * alice.alchemist.model.implementations.actions.SAPEREAgent#cloneOnNewNode
     * (alice.alchemist.model.interfaces.Node,
     * alice.alchemist.model.interfaces.Reaction)
     */
    @Override
    public LsaCountNeighborsAction cloneAction(final Node<List<ILsaMolecule>> node, final Reaction<List<ILsaMolecule>> reaction) {
        return new LsaCountNeighborsAction(getEnvironment(), (ILsaNode) node, mol, countVarName, rnd);
    }

    @Override
    public void execute() {
        final List<IExpression> l = mol.allocateVar(getMatches());
        Double num = 0.0;
        environment.getNeighborhood(getNode());
        for (final Node<List<ILsaMolecule>> nod : environment.getNeighborhood(getNode()).getNeighbors()) {
            if (!nod.getConcentration(new LsaMolecule(l)).isEmpty()) {
                num++;
            }
        }
        getMatches().put(countVarName, new NumTreeNode(num));
    }

    /**
     * @return the current environment
     */
    private Environment<List<ILsaMolecule>, ?> getEnvironment() {
        return environment;
    }

    /*
     * (non-Javadoc)
     *
     * @see alice.alchemist.model.implementations.actions.SAPEREAgent#toString()
     */
    @Override
    public String toString() {
        return "Count " + countVarName;
    }

}
