/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.Node;

import it.unibo.alchemist.model.Reaction;
import org.danilopianini.lang.HashString;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;


/**
 */
public final class LsaNeighborhoodCondition extends LsaStandardCondition {

    private static final long serialVersionUID = 5472803597473997104L;
    private final Environment<List<ILsaMolecule>, ?> environment;

    /**
     * @param node the node
     * @param molecule the molecule
     * @param environment the environment
     */
    public LsaNeighborhoodCondition(
            final ILsaNode node,
            final ILsaMolecule molecule,
            final Environment<List<ILsaMolecule>, ?> environment
    ) {
        super(molecule, node);
        this.environment = environment;
    }

    @Override
    public LsaNeighborhoodCondition cloneCondition(final Node<List<ILsaMolecule>> node, final Reaction<List<ILsaMolecule>> r) {
        return new LsaNeighborhoodCondition((ILsaNode) node, getMolecule(), environment);
    }

    @Override
    public boolean filter(
            final List<Map<HashString, ITreeNode<?>>> matchesList,
            final List<ILsaNode> validNodes,
            final List<Map<ILsaNode, List<ILsaMolecule>>> retrieved
    ) {
        if (validNodes.isEmpty()) {
            return false;
        }
        if (matchesList.isEmpty()) {
            /*
             * This is the first condition. It must create all the matches.
             * 
             * To create them, it must check every neighbor, and create the
             * matches for each of them.
             */
            int lastSize = 0;
            for (int i = 0; i < validNodes.size();) {
                final ILsaNode neigh = validNodes.get(i);
                createMatches(getMolecule(), neigh, matchesList, retrieved);
                if (matchesList.size() > lastSize) {
                    /*
                     * This neighbor has the LSA we are checking for, so new
                     * matches have been created. For each of them, we must add
                     * the selected node special property, in order for the
                     * actions on single neighbor to be correctly bound.
                     */
                    final NumTreeNode nodeId = new NumTreeNode(neigh.getId());
                    for (; lastSize < matchesList.size(); lastSize++) {
                        matchesList.get(lastSize).put(LsaMolecule.SYN_SELECTED, nodeId);
                    }
                    i++;
                } else {
                    /*
                     * This neighbor is not valid, and thus should be removed
                     * from the validNodes list.
                     */
                    validNodes.remove(i);
                }
            }
            /*
             * True if at least a valid match has been created.
             */
            return makeValid(!matchesList.isEmpty());
        }
        /*
         * At least a condition has been run before. This condition must check
         * the existing matches, removing all those which are no longer valid.
         */
        boolean matchesfound = false;
        final AbstractSet<ILsaNode> newValidNodes = new LinkedHashSet<>(validNodes.size());
        for (int i = matchesList.size() - 1; i >= 0; i--) {
            final Map<ILsaNode, List<ILsaMolecule>> alreadyRemovedMap = retrieved.get(i);
            final Map<HashString, ITreeNode<?>> matches = matchesList.get(i);
            final List<IExpression> partialInstance = getMolecule().allocateVar(matches);
            final boolean dups = getMolecule().hasDuplicateVariables();
            /*
             * There is a chance that other Neighborhood conditions have been
             * run before. Thus, it is mandatory to check if the selected node
             * match has been instanced, and in case just run on that node.
             */
            final ITreeNode<?> node = matches.get(LsaMolecule.SYN_SELECTED);
            if (node != null) {
                final int id = ((Double) node.getData()).intValue();
                for (int j = validNodes.size() - 1; j >= 0; j--) {
                    final ILsaNode n = validNodes.get(j);
                    if (n.getId() == id) {
                        List<ILsaMolecule> alreadyRemoved = alreadyRemovedMap.get(n);
                        if (alreadyRemoved == null) {
                            alreadyRemoved = new ArrayList<>();
                            alreadyRemovedMap.put(n, alreadyRemoved);
                        }
                        final List<ILsaMolecule> otherMatches =
                                calculateMatches(partialInstance, dups, n.getLsaSpace(), alreadyRemoved);
                        if (otherMatches.isEmpty()) {
                            /*
                             * This match should be removed, but there might be
                             * other matches in which this node is still valid,
                             * so the node validity check must be performed
                             * after.
                             */
                            retrieved.remove(i);
                            matchesList.remove(i);
                        } else {
                            incorporateNewMatches(
                                    n,
                                    otherMatches,
                                    matches,
                                    getMolecule(),
                                    matchesList,
                                    alreadyRemovedMap,
                                    retrieved
                            );
                            matchesfound = true;
                            newValidNodes.add(n);
                        }
                        break;
                    }
                }
            } else {
                /*
                 * No node has been selected for this match yet
                 */
                final Map<ILsaNode, List<ILsaMolecule>> matchesPerNode = new HashMap<>();
                for (int j = validNodes.size() - 1; j >= 0; j--) {
                    final ILsaNode n = validNodes.get(j);
                    List<ILsaMolecule> alreadyRemoved = alreadyRemovedMap.get(n);
                    if (alreadyRemoved == null) {
                        alreadyRemoved = new ArrayList<>();
                        alreadyRemovedMap.put(n, alreadyRemoved);
                    }
                    final List<ILsaMolecule> otherMatches =
                            calculateMatches(partialInstance, dups, n.getLsaSpace(), alreadyRemoved);
                    if (!otherMatches.isEmpty()) {
                        matchesPerNode.put(n, otherMatches);
                        newValidNodes.add(n);
                    }
                }
                if (matchesPerNode.isEmpty()) {
                    /*
                     * I've checked all the neighbors which are still valid. If
                     * this condition is not valid for the current match, it
                     * should be removed.
                     */
                    matchesList.remove(i);
                } else {
                    incorporateNewMatches(matchesPerNode, matches, getMolecule(), matchesList, alreadyRemovedMap, retrieved);
                    matchesfound = true;
                }
            }
        }
        /*
         * Valid nodes redefinition.
         */
        for (int i = 0; i < validNodes.size(); i++) {
            if (!newValidNodes.contains(validNodes.get(i))) {
                validNodes.remove(i);
                i--;
            }
        }
        return makeValid(matchesfound);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * alice.alchemist.model.implementations.conditions.LsaStandardCondition
     * #getContext()
     */
    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    /**
     * @return the current environment
     */
    protected Environment<List<ILsaMolecule>, ?> getEnvironment() {
        return environment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * alice.alchemist.model.implementations.conditions.LsaStandardCondition
     * #toString()
     */
    @Override
    public String toString() {
        return "+" + super.toString();
    }

}
