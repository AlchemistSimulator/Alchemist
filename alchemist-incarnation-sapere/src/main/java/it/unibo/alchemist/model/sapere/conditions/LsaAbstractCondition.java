/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.conditions;

import it.unibo.alchemist.model.sapere.dsl.impl.ListTreeNode;
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode;
import it.unibo.alchemist.model.sapere.dsl.impl.Type;
import it.unibo.alchemist.model.sapere.dsl.impl.UIDNode;
import it.unibo.alchemist.model.sapere.dsl.IExpression;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import it.unibo.alchemist.model.conditions.AbstractCondition;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaCondition;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import it.unibo.alchemist.model.Node;

import it.unibo.alchemist.model.Reaction;
import org.danilopianini.lang.HashString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 */
public abstract class LsaAbstractCondition extends AbstractCondition<List<ILsaMolecule>> implements ILsaCondition {

    private static final long serialVersionUID = -5633486241371700913L;

    /**
     * @param node
     *            the node hosting this action
     * @param m
     *            the set of molecules on which this actions act
     */
    public LsaAbstractCondition(final ILsaNode node, final Set<ILsaMolecule> m) {
        super(node);
        for (final ILsaMolecule mol : m) {
            declareDependencyOn(mol);
        }
    }

    @Override
    public abstract String toString();

    @Override
    public final ILsaNode getNode() {
        return (ILsaNode) super.getNode();
    }

    @Override
    public abstract LsaAbstractCondition cloneCondition(Node<List<ILsaMolecule>> node, Reaction<List<ILsaMolecule>> reaction);

    /**
     * @param partialInstance
     *            the template, possibly partly instanced
     * @param duplicateVariables
     *            true if partialInstance contains the same variable multiple
     *            times
     * @param lsaSpace
     *            the LSA space of the node on which this function is working on
     * @param alreadyRemoved
     *            the list of the molecules already removed from this node.
     * @return the list of molecules in this LSA space that match the
     *         partialInstance, excluding all those which have been already
     *         removed.
     */
    protected static List<ILsaMolecule> calculateMatches(
            final List<IExpression> partialInstance,
            final boolean duplicateVariables,
            final List<ILsaMolecule> lsaSpace,
            final List<ILsaMolecule> alreadyRemoved
    ) {
        final List<ILsaMolecule> l = new ArrayList<>(lsaSpace.size() - alreadyRemoved.size());
        for (final ILsaMolecule matched : lsaSpace) {
            if (matched.matches(partialInstance, duplicateVariables)
                    && countElements(lsaSpace, matched) > countElements(alreadyRemoved, matched)) {
                l.add(matched);
            }
        }
        return l;
    }

    private static <T> int countElements(final Collection<T> l, final T o) {
        int count = 0;
        for (final T t : l) {
            if (t.equals(o)) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param template
     *            the template molecule
     * @param node
     *            the node on which this function is working
     * @param matchesList
     *            the list of matches to populate (theoretically, it should
     *            always be empty when calling this method)
     * @param retrieved
     *            the list of the molecules removed from each node
     *            (theoretically, it should always be empty when calling this
     *            method)
     */
    protected static void createMatches(
            final ILsaMolecule template,
            final ILsaNode node,
            final List<Map<HashString, ITreeNode<?>>> matchesList,
            final List<Map<ILsaNode, List<ILsaMolecule>>> retrieved
    ) {
        final List<ILsaMolecule> lsaSpace = node.getLsaSpace();
        for (final ILsaMolecule matched : lsaSpace) {
            if (template.matches(matched)) {
                /*
                 * If a match is found, the matched LSA must be added to the
                 * list of removed items corresponding to the match, the matches
                 * map must be created, the map should be added to the possible
                 * matches list, and the index must not be increased.
                 */
                final Map<HashString, ITreeNode<?>> matches = new HashMap<>(matched.argsNumber() * 2 + 1, 1f);
                matches.put(LsaMolecule.SYN_MOL_ID, new UIDNode(matched.toHashString()));
                updateMap(matches, matched, template);
                matchesList.add(matches);
                final List<ILsaMolecule> modifiedSpace = new ArrayList<>(lsaSpace.size());
                modifiedSpace.add(matched);
                final Map<ILsaNode, List<ILsaMolecule>> retrievedInThisNode = new HashMap<>(lsaSpace.size(), 1f);
                retrievedInThisNode.put(node, modifiedSpace);
                retrieved.add(retrievedInThisNode);
            }
        }
    }

    /**
     * Updates the matches map by adding the mapping between the variables of
     * template and the contents of instance.
     * 
     * @param map
     *            : the map to update
     * @param instance
     *            : LsaMolecule instance (contain variable values)
     * @param template
     *            : LsaMolecule template (contain variable names)
     */
    protected static void updateMap(
            final Map<HashString, ITreeNode<?>> map,
            final Iterable<IExpression> instance,
            final ILsaMolecule template
    ) {
        /*
         * Iterate over inst
         */
        int i = 0;
        for (final IExpression instarg : instance) {
            final IExpression molarg = template.getArg(i);
            if (molarg.getRootNodeType() == Type.VAR) {
                /*
                 * If it's a variable
                 */
                map.put((HashString) molarg.getRootNodeData(), instarg.getRootNode());
            } else {
                /*
                 * If it's a comparator
                 */
                if (molarg.getRootNodeType() == Type.COMPARATOR) {
                    if (molarg.getAST().getRoot().getLeftChild().getType() == Type.VAR) {
                        map.put(molarg.getLeftChildren().toHashString(), instarg.getRootNode());
                    }
                } else if (molarg.getRootNodeType() == Type.LISTCOMPARATOR) {
                    if (molarg.getAST().getRoot().getLeftChild().getType() == Type.VAR
                            && instarg.getRootNodeData() instanceof Set<?>) {
                        map.put(molarg.getLeftChildren().toHashString(), instarg.getRootNode());

                    }
                } else if (molarg.getRootNodeType() == Type.LIST) {
                    /*
                     * Assignment of variables within lists.
                     */
                    final Set<ITreeNode<?>> molList = ((ListTreeNode) molarg.getAST().getRoot()).getData();
                    final Iterator<ITreeNode<?>> instList = ((ListTreeNode) instarg.getAST().getRoot()).getData().iterator();
                    for (final ITreeNode<?> var : molList) {
                        if (var.getType() == Type.VAR) {
                            map.put(var.toHashString(), instList.next());
                        }
                    }
                }
            }
            i++;
        }

    }

    /**
     * @param node
     *            The node on which the condition has been verified
     * @param otherMatches
     *            The list of new matches that have been calculated
     * @param oldMatches
     *            The previous matches
     * @param template
     *            The molecule template which has been verified
     * @param matchesList
     *            The list of all the valid matches. If more than one valid
     *            match has been found, new entries will be added
     * @param alreadyRemoved
     *            the map of the lists of molecules already removed from each
     *            node for the current match
     * @param retrieved
     *            the list of all the maps that lists the molecules removed from
     *            each node
     */
    protected static void incorporateNewMatches(
            final ILsaNode node, final List<ILsaMolecule> otherMatches,
            final Map<HashString, ITreeNode<?>> oldMatches,
            final ILsaMolecule template,
            final List<Map<HashString, ITreeNode<?>>> matchesList,
            final Map<ILsaNode, List<ILsaMolecule>> alreadyRemoved,
            final List<Map<ILsaNode, List<ILsaMolecule>>> retrieved
    ) {
        for (int j = 1; j < otherMatches.size(); j++) {
            final ILsaMolecule instance = otherMatches.get(j);
            /*
             * Make copies of the match under analysis, then populate them with
             * the new matches
             */
            final Map<HashString, ITreeNode<?>> newMap = new HashMap<>(oldMatches);
            updateMap(newMap, instance, template);
            matchesList.add(newMap);

            final Map<ILsaNode, List<ILsaMolecule>> contentMap = new HashMap<>(alreadyRemoved);
            final List<ILsaMolecule> oldRetrieved = contentMap.get(node);
            /*
             * If this node already has some modified molecule, copy them.
             * Otherwise, create a new list.
             */
            final List<ILsaMolecule> newRetrieved = oldRetrieved == null ? new ArrayList<>() : new ArrayList<>(oldRetrieved);
            newRetrieved.add(instance);
            contentMap.put(node, newRetrieved);
            retrieved.add(contentMap);
        }
        /*
         * Now, update the matches for the first entry
         */
        final ILsaMolecule instance = otherMatches.get(0);
        updateMap(oldMatches, instance, template);
        List<ILsaMolecule> alreadyRetrievedInThisNode = alreadyRemoved.get(node);
        if (alreadyRetrievedInThisNode == null) {
            alreadyRetrievedInThisNode = new ArrayList<>();
            alreadyRetrievedInThisNode.add(instance);
            alreadyRemoved.put(node, alreadyRetrievedInThisNode);
        } else {
            alreadyRetrievedInThisNode.add(instance);
        }
    }

    /**
     * This has to be used to incorporate new matches when the they are
     * node-specific and available for multiple nodes.
     * 
     * @param otherMatchesMap
     *            Other matches map
     * @param oldMatches
     *            The previous matches
     * @param template
     *            The molecule template which has been verified
     * @param matchesList
     *            The list of all the valid matches. If more than one valid
     *            match has been found, new entries will be added
     * @param alreadyRemoved
     *            the map of the lists of molecules already removed from each
     *            node for the current match
     * @param retrieved
     *            the list of all the maps that lists the molecules removed from
     *            each node
     */
    protected static void incorporateNewMatches(
            final Map<ILsaNode, List<ILsaMolecule>> otherMatchesMap,
            final Map<HashString, ITreeNode<?>> oldMatches,
            final ILsaMolecule template,
            final List<Map<HashString, ITreeNode<?>>> matchesList,
            final Map<ILsaNode, List<ILsaMolecule>> alreadyRemoved,
            final List<Map<ILsaNode, List<ILsaMolecule>>> retrieved
    ) {
        for (final Entry<ILsaNode, List<ILsaMolecule>> e : otherMatchesMap.entrySet()) {
            final ILsaNode node = e.getKey();
            final List<ILsaMolecule> otherMatches = e.getValue();
            for (final ILsaMolecule instance : otherMatches) {
                /*
                 * Make copies of the match under analysis, then populate them
                 * with the new matches
                 */
                final Map<HashString, ITreeNode<?>> newMap = new HashMap<>(oldMatches);
                updateMap(newMap, instance, template);
                newMap.put(LsaMolecule.SYN_SELECTED, new NumTreeNode(node.getId()));
                matchesList.add(newMap);
                final Map<ILsaNode, List<ILsaMolecule>> contentMap = new HashMap<>(alreadyRemoved);
                final List<ILsaMolecule> oldRetrieved = contentMap.get(node);
                /*
                 * If this node already has some modified molecule, copy them.
                 * Otherwise, create a new list.
                 */
                final List<ILsaMolecule> newRetrieved = oldRetrieved == null
                        ? new ArrayList<>(node.getLsaSpace().size())
                        : new ArrayList<>(oldRetrieved);
                newRetrieved.add(instance);
                contentMap.put(node, newRetrieved);
                retrieved.add(contentMap);
            }
        }
        /*
         * Remove the original entry.
         */
        final int i = matchesList.indexOf(oldMatches);
        matchesList.remove(i);
        retrieved.remove(i);
    }
}
