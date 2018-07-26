/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import org.apache.commons.math3.random.RandomGenerator;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import org.danilopianini.lang.HashString;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;


/**
 */
public class LsaCountNeighborsAction extends SAPERELocalAgent {

    private static final long serialVersionUID = -7128058274012426458L;
    private final HashString countVarName;
    private final Environment<List<ILsaMolecule>, ?> env;
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
     *            to matches map)
     * @param rand
     *            Random engine
     */
    public LsaCountNeighborsAction(final Environment<List<ILsaMolecule>, ?> environment, final ILsaNode node, final ILsaMolecule molToCount, final HashString countVar, final RandomGenerator rand) {
        super(node);
        rnd = rand;
        env = environment;
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
     *            to matches map)
     * @param rand
     *            Random engine
     */
    public LsaCountNeighborsAction(final Environment<List<ILsaMolecule>, ?> environment, final ILsaNode node, final ILsaMolecule molToCount, final String countVar, final RandomGenerator rand) {
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
    public LsaCountNeighborsAction cloneAction(final Node<List<ILsaMolecule>> n, final Reaction<List<ILsaMolecule>> r) {
        return new LsaCountNeighborsAction(getEnvironment(), (ILsaNode) n, mol, countVarName, rnd);
    }

    @Override
    public void execute() {
        final List<IExpression> l = mol.allocateVar(getMatches());
        Double num = 0.0;
        if (env.getNeighborhood(getNode()) != null) {
            for (Node<List<ILsaMolecule>> nod : env.getNeighborhood(getNode()).getNeighbors()) {
                nod = (LsaNode) nod;
                if (nod.getConcentration(new LsaMolecule(l)).size() != 0) {
                    num++;
                }
            }
        }
        getMatches().put(countVarName, new NumTreeNode(num));
    }

    /**
     * @return the current environment
     */
    protected Environment<List<ILsaMolecule>, ?> getEnvironment() {
        return env;
    }

    /**
     * @return a new random double
     */
    protected double random() {
        return rnd.nextDouble();
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
