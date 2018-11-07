/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

import org.danilopianini.lang.HashString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Sets;

/**
 * simple LSA-condition (example: <grad,X,1>). Search an instance of a template
 * in a node. The LSAMolecule matched, if exist, will not be deleted from the
 * node Lsa-space . It can be deleted from the reaction, if necessary.
 */
public class LsaStandardCondition extends LsaAbstractCondition {

    private static final long serialVersionUID = -7400434133059391639L;

    private final ILsaMolecule molecule;
    private boolean valid;

    /**
     * Builds an LsaStandardCondition.
     * 
     * @param mol
     *            the molecole whose presence must be tested
     * @param n
     *            the node in which this condition will act
     */
    public LsaStandardCondition(final ILsaMolecule mol, final ILsaNode n) {
        super(n, Sets.newHashSet(new ILsaMolecule[] { mol }));
        molecule = mol;
    }

    @Override
    public LsaStandardCondition cloneCondition(final Node<List<ILsaMolecule>> n, final Reaction<List<ILsaMolecule>> r) {
        return new LsaStandardCondition(molecule, (ILsaNode) n);
    }

    @Override
    public boolean filter(
            final List<Map<HashString, ITreeNode<?>>> matchesList,
            final List<ILsaNode> validNodes,
            final List<Map<ILsaNode, List<ILsaMolecule>>> retrieved) {
        final ILsaNode node = getNode();
        if (matchesList.isEmpty()) {
            /*
             * This is the first condition. It must create all the matches.
             */
            createMatches(molecule, node, matchesList, retrieved);
            return setValid(!matchesList.isEmpty());
        }
        /*
         * At least a condition has been run before. This condition must check
         * the existing matches, removing all those which are no longer valid.
         */
        boolean matchesfound = false;
        /*
         * I run through the list from backwards, so I do not analyze newly
         * added matches
         */
        for (int i = matchesList.size() - 1; i >= 0; i--) {
            final Map<ILsaNode, List<ILsaMolecule>> alreadyRemoved = retrieved.get(i);
            List<ILsaMolecule> alreadyRemovedInThisNode = alreadyRemoved.get(node);
            if (alreadyRemovedInThisNode == null) {
                alreadyRemovedInThisNode = new ArrayList<>();
                alreadyRemoved.put(node, alreadyRemovedInThisNode);
            }
            final Map<HashString, ITreeNode<?>> matches = matchesList.get(i);
            final List<IExpression> partialInstance = molecule.allocateVar(matches);
            final boolean dups = molecule.hasDuplicateVariables();
            /*
             * There are three possibilities:
             * 
             * 1 - No valid combinations are found. It implies that this match
             * and the relative entry in newSpaces should be deleted.
             * 
             * 2 - Only a single valid match is found. Both matches and
             * newSpaces must be modified.
             * 
             * 3 - After two, other valid matches are found. In this case, a new
             * entry in matchesList and newSpaces should be created.
             */
            final List<ILsaMolecule> otherMatches = calculateMatches(partialInstance, dups,
                    node.getLsaSpace(), alreadyRemovedInThisNode);
            if (otherMatches.isEmpty()) {
                retrieved.remove(i);
                matchesList.remove(i);
            } else {
                /*
                 * Deal with all the new matches
                 */
                incorporateNewMatches(getNode(), otherMatches, matches, molecule, matchesList, alreadyRemoved, retrieved);
                matchesfound = true;
            }
        }
        return setValid(matchesfound);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    /**
     * @return the molecule template whose presence is tested by this condition
     */
    protected ILsaMolecule getMolecule() {
        return molecule;
    }

    @Override
    public double getPropensityContribution() {
        return -1;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return molecule.toString();
    }

    /**
     * Allows to set the validity value for this condition by subclasses. Handle
     * with care.
     * 
     * @param isValid
     *            true if this condition is valid
     * @return the value which is passed.
     */
    protected boolean setValid(final boolean isValid) {
        valid = isValid;
        return valid;
    }

}
