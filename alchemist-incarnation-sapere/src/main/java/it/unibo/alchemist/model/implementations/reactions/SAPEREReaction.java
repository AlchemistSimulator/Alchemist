/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.interfaces.Dependency;
import it.unibo.alchemist.model.interfaces.Reaction;
import org.apache.commons.math3.random.RandomGenerator;
import it.unibo.alchemist.model.implementations.actions.LsaStandardAction;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.timedistributions.SAPERETimeDistribution;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaAction;
import it.unibo.alchemist.model.interfaces.ILsaCondition;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import org.danilopianini.lang.HashString;
import org.danilopianini.util.ArrayListSet;
import org.danilopianini.util.ListSet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * This class realizes a reaction with Lsa concentrations.
 * 
 * 
 */
@SuppressWarnings("unchecked")
public final class SAPEREReaction extends AbstractReaction<List<ILsaMolecule>> {

    private static final long serialVersionUID = 1L;

    private final Environment<List<ILsaMolecule>, ?> environment;
    @SuppressFBWarnings(
            value = "SE_BAD_FIELD",
            justification = "All provided RandomGenerator implementations are actually Serializable"
    )
    private final RandomGenerator rng;
    private final SAPERETimeDistribution timeDistribution;

    private boolean emptyExecution;
    private boolean modifiesOnlyLocally = true;
    private List<Map<HashString, ITreeNode<?>>> possibleMatches = new ArrayList<>(0);
    private List<Map<ILsaNode, List<ILsaMolecule>>> possibleRemove = new ArrayList<>(0);
    private List<Double> propensities = new ArrayList<>(0);
    private double totalPropensity;
    private List<ILsaNode> validNodes = new ArrayList<>(0);

    /**
     * This method screens the lsaMolecule list, deleting all molecule which can
     * be covered from another one more generic.
     * 
     * @param moleculeList
     *            List of lsaMolecule to screen
     */
    private static void screen(final List<Dependency> moleculeList) {
        /*
         * PHASE 1: generalize the list
         */
        for (int i = 0; i < moleculeList.size(); i++) { // NOPMD: this loop has side effects
            moleculeList.add(((ILsaMolecule) moleculeList.remove(0)).generalize());
        }
        /*
         * PHASE2: compare one-by-one
         */
        for (int i = moleculeList.size() - 1; i > 0; i--) {
            for (int p = i - 1; p >= 0; p--) {
                final ILsaMolecule m1 = (ILsaMolecule) moleculeList.get(i);
                final ILsaMolecule m2 = (ILsaMolecule) moleculeList.get(p);
                if (m2.equals(m1) || m2.moreGenericOf(m1)) {
                    moleculeList.remove(i);
                    i--;
                } else if (m1.moreGenericOf(m2)) {
                    moleculeList.remove(p);
                    i--;
                }
            }
        }
    }

    /**
     * @param environment
     *            the current environment
     * @param node
     *            the current node
     * @param randomGenerator
     *            the random engine to use
     * @param timeDistribution
     *            Time Distribution
     */
    public SAPEREReaction(
            final Environment<List<ILsaMolecule>, ?> environment,
            final ILsaNode node,
            final RandomGenerator randomGenerator,
            final TimeDistribution<List<ILsaMolecule>> timeDistribution
    ) {
        super(node, timeDistribution);
        if (getTimeDistribution() instanceof SAPERETimeDistribution) {
            this.timeDistribution = (SAPERETimeDistribution) getTimeDistribution();
        } else {
            this.timeDistribution = null;
        }
        rng = randomGenerator;
        this.environment = environment;
    }

    @Override
    public Reaction<List<ILsaMolecule>> cloneOnNewNode(final Node<List<ILsaMolecule>> node, final Time currentTime) {
        final var timeDistributionClone = timeDistribution.cloneOnNewNode(node, currentTime);
        final SAPEREReaction res = new SAPEREReaction(environment, (ILsaNode) node, rng, timeDistributionClone);
        final ArrayList<Condition<List<ILsaMolecule>>> c = new ArrayList<>();
        for (final Condition<List<ILsaMolecule>> cond : getConditions()) {
            c.add(cond.cloneCondition(node, res));
        }
        final ArrayList<Action<List<ILsaMolecule>>> a = new ArrayList<>();
        for (final Action<List<ILsaMolecule>> act : getActions()) {
            a.add(act.cloneAction(node, res));
        }
        res.setActions(a);
        res.setConditions(c);
        return res;
    }

    /**
     * @return the inner {@link Action} list, cast
     */
    protected List<ILsaAction> getSAPEREActions() {
        return (List<ILsaAction>) (List<? extends Action<List<ILsaMolecule>>>) getActions();
    }

    /**
     * @return the inner {@link Condition} list, cast
     */
    protected List<ILsaCondition> getSAPEREConditions() {
        return (List<ILsaCondition>) (List<? extends Condition<List<ILsaMolecule>>>) getConditions();
    }

    @Override
    public void execute() {
        if (possibleMatches.isEmpty()) {
            for (final ILsaAction a : getSAPEREActions()) {
                a.setExecutionContext(null, validNodes);
                a.execute();
            }
            return;
        }
        final Position<?> nodePosCache = modifiesOnlyLocally ? environment.getPosition(getNode()) : null;
        final List<? extends ILsaMolecule> localContentCache = modifiesOnlyLocally
                ? new ArrayList<>(getLsaNode().getLsaSpace())
                : null;
        Map<HashString, ITreeNode<?>> matches = null;
        Map<ILsaNode, List<ILsaMolecule>> toRemove = null;
        /*
         * If there is infinite propensity, the last match added is the one to
         * choose, since it is the one which generated the "infinity" value.
         */
        if (totalPropensity == Double.POSITIVE_INFINITY) {
            final int index = possibleMatches.size() - 1;
            matches = possibleMatches.get(index);
            toRemove = possibleRemove.get(index);
        } else if (numericRate()) {
            /*
             * If the rate is numeric, the choice is just random
             */
            final int index = Math.abs(rng.nextInt()) % possibleMatches.size();
            matches = possibleMatches.get(index);
            toRemove = possibleRemove.get(index);
        } else {
            /*
             * Otherwise, the matches must be chosen randomly using their
             * propensities
             */
            final double index = rng.nextDouble() * totalPropensity;
            double sum = 0;
            for (int i = 0; matches == null; i++) {
                sum += propensities.get(i);
                if (sum > index) {
                    matches = possibleMatches.get(i);
                    toRemove = possibleRemove.get(i);
                }
            }
        }
        /*
         * The matched LSAs must be removed from the local space, if no action
         * added them back.
         */
        for (final Entry<ILsaNode, List<ILsaMolecule>> entry : toRemove.entrySet()) {
            final ILsaNode n = entry.getKey();
            for (final ILsaMolecule m : entry.getValue()) {
                n.removeConcentration(m);
            }
        }
        /*
         * #T Must be loaded by the reaction, which is the only structure aware
         * of the time. Other special values (#NEIG, #O, #D) will be allocated
         * inside the actions.
         */
        matches.put(LsaMolecule.SYN_T, new NumTreeNode(getTau().toDouble()));
        for (final ILsaAction a : getSAPEREActions()) {
            a.setExecutionContext(matches, validNodes);
            a.execute();
        }

        /*
         * Empty action optimization
         */
        if (modifiesOnlyLocally) {
            final ILsaNode n = getLsaNode();
            if (Objects.requireNonNull(nodePosCache).equals(environment.getPosition(getNode()))) {
                final List<? extends ILsaMolecule> contents = n.getLsaSpace();
                if (contents.size() == Objects.requireNonNull(localContentCache).size()) {
                    emptyExecution = localContentCache.containsAll(contents);
                }
            }
        }
    }

    /**
     * @return the current environment
     */
    protected Environment<List<ILsaMolecule>, ?> getEnvironment() {
        return environment;
    }

    /**
     * @return the local {@link Node} as {@link ILsaNode}
     */
    protected ILsaNode getLsaNode() {
        return (ILsaNode) super.getNode();
    }

    /**
     * @return the list of all possible matches
     */
    protected List<Map<HashString, ITreeNode<?>>> getPossibleMatches() {
        return possibleMatches;
    }

    /**
     * @return the list of molecules which would be removed for each node if the
     *         corresponding match would be chosen
     */
    protected List<Map<ILsaNode, List<ILsaMolecule>>> getPossibleRemove() {
        return possibleRemove;
    }

    /**
     * @return the list of the propensities computed for each match combination
     */
    protected List<Double> getPropensities() {
        return propensities;
    }

    @Override
    protected void updateInternalStatus(
            final Time currentTime,
            final boolean hasBeenExecuted,
            final Environment<List<ILsaMolecule>, ?> environment
    ) {
        if (emptyExecution) {
            emptyExecution = false;
            totalPropensity = 0;
        } else {
            /*
             * Valid nodes must be re-initialized, as per issue #
             */
            final Collection<? extends Node<List<ILsaMolecule>>> neighs =
                    this.environment.getNeighborhood(getNode()).getNeighbors();
            validNodes = new ArrayList<>(neighs.size());
            for (final Node<List<ILsaMolecule>> neigh: neighs) {
                validNodes.add((ILsaNode) neigh);
            }
            if (getConditions().isEmpty()) {
                totalPropensity = getTimeDistribution().getRate();
            } else {
                totalPropensity = 0d;
                possibleMatches = new ArrayList<>();
                propensities = new ArrayList<>();
                possibleRemove = new ArrayList<>();
                /*
                 * Apply all the conditions as filters
                 */
                for (final ILsaCondition cond : getSAPEREConditions()) {
                    if (!cond.filter(possibleMatches, validNodes, possibleRemove)) {
                        /*
                         * It is supposed that a condition fails if it must put null
                         * in the filter lists, so null values are not expected.
                         */
                        return;
                    }
                }
                if (numericRate()) {
                    totalPropensity = possibleMatches.size() * getTimeDistribution().getRate();
                } else {
                    /*
                     * For each possible match, compute the propensity
                     */
                    for (final Map<HashString, ITreeNode<?>> match : possibleMatches) {
                        timeDistribution.setMatches(match);
                        final double p = timeDistribution.getRate();
                        propensities.add(p);
                        totalPropensity += p;
                        if (totalPropensity == Double.POSITIVE_INFINITY) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean numericRate() {
        return timeDistribution == null || timeDistribution.isStatic();
    }

    @Override
    public double getRate() {
        return totalPropensity;
    }

    @Override
    public String getRateAsString() {
        return numericRate() ? Double.toString(getTimeDistribution().getRate()) : timeDistribution.getRateEquation().toString();
    }

    /**
     * @return the aggregated propensity of all the possible instances of this
     *         reaction
     */
    protected double getTotalPropensity() {
        return totalPropensity;
    }

    /**
     * @return the list of nodes which are valid for this reaction
     */
    protected List<ILsaNode> getValidNodes() {
        return validNodes;
    }

    @Override
    public void setActions(final List<? extends Action<List<ILsaMolecule>>> actions) {
        setConditionsAndActions(getConditions(), actions);
    }

    @Override
    public void setConditions(final List<? extends Condition<List<ILsaMolecule>>> conditions) {
        setConditionsAndActions(conditions, getActions());
    }

    private void setConditionsAndActions(
            final List<? extends Condition<List<ILsaMolecule>>> c,
            final List<? extends Action<List<ILsaMolecule>>> a
    ) {
        super.setConditions((List<Condition<List<ILsaMolecule>>>) c);
        super.setActions((List<Action<List<ILsaMolecule>>>) a);
        modifiesOnlyLocally = getOutputContext() == Context.LOCAL;
        /*
         * The following optimization only makes sense if the reaction acts
         * locally. Otherwise there is no control on where the modified
         * molecules will end up.
         */
        final ListSet<Dependency> inboundDependencies = new ArrayListSet<>(getInboundDependencies());
        final ListSet<Dependency> outboundDependencies = new ArrayListSet<>(getOutboundDependencies());
        if (getInputContext() == Context.LOCAL && modifiesOnlyLocally) {
            /*
             * Moreover, since there is no control over the personalised agents,
             * it's required to check that all the actions are the standard
             * manipulations.
             */
            boolean allStandard = true;
            for (final Action<List<ILsaMolecule>> act : getActions()) {
                if (!(act instanceof LsaStandardAction)) {
                    allStandard = false;
                    break;
                }
            }
            if (allStandard) {
                for (final Dependency m : inboundDependencies) {
                    /*
                     * For each influencing molecule:
                     * 
                     * If it appears identically on both sides of the reaction,
                     * then it can be ignored when calculating the dependencies
                     * 
                     * If there are some molecules on the left side which are
                     * not on the right side, they should be added (they will be
                     * removed)
                     */
                    if (outboundDependencies.contains(m)) {
                        outboundDependencies.remove(m);
                    } else {
                        outboundDependencies.add(m);
                    }
                }
            }
        }
        screen(inboundDependencies);
        screen(outboundDependencies);
        inboundDependencies.forEach(this::addInboundDependency);
        outboundDependencies.forEach(this::addOutboundDependency);
    }

    /**
     * @param pm
     *            the list of all possible matches
     */
    protected void setPossibleMatches(final List<Map<HashString, ITreeNode<?>>> pm) {
        this.possibleMatches = pm;
    }

    /**
     * @param possibleRemoved
     *            the list of molecules which would be removed for each node if
     *            the corresponding match would be chosen
     */
    protected void setPossibleRemove(final List<Map<ILsaNode, List<ILsaMolecule>>> possibleRemoved) {
        this.possibleRemove = possibleRemoved;
    }

    /**
     * @param p
     *            the list of the propensities computed for each match
     *            combination
     */
    protected void setPropensities(final List<Double> p) {
        this.propensities = p;
    }

    /**
     * @param tp
     *            the aggregated propensity of all the possible instances of
     *            this reaction
     */
    protected void setTotalPropensity(final double tp) {
        this.totalPropensity = tp;
    }

    /**
     * @param valid
     *            the list of nodes which are valid for this reaction
     */
    protected void setValidNodes(final List<ILsaNode> valid) {
        this.validNodes = valid;
    }

}
