/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
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
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import org.danilopianini.lang.util.FasterString;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class realizes a reaction with Lsa concentrations.
 * 
 * 
 */
@SuppressWarnings("unchecked")
public class SAPEREReaction extends AReaction<List<? extends ILsaMolecule>> {

    private static final long serialVersionUID = -7264856859267079626L;
    private static final String CAST_NEEDED = "There is a necessary cast.";
    private static final String USM_USELESS_SUBCLASS_METHOD = "USM_USELESS_SUBCLASS_METHOD";

    private final Environment<List<? extends ILsaMolecule>> environment;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All provided RandomGenerator implementations are actually Serializable")
    private final RandomGenerator rng;
    private final SAPERETimeDistribution timedist;

    private boolean emptyExecution;
    private boolean modifiesOnlyLocally = true;
    private List<Map<FasterString, ITreeNode<?>>> possibleMatches = new ArrayList<>(0);
    private List<Map<ILsaNode, List<ILsaMolecule>>> possibleRemove = new ArrayList<>(0);
    private List<Double> propensities = new ArrayList<>(0);
    private double totalPropensity;
    private List<ILsaNode> validNodes = new ArrayList<>(0);

    /**
     * This method screens the lsaMolecule list, deleting all molecule which can
     * be covered from another one more generic.
     * 
     * @param listmol
     *            List of lsaMolecule to screen
     */
    private static void screen(final List<ILsaMolecule> listmol) {
        /*
         * PHASE 1: generalize the list
         */
        for (int i = 0; i < listmol.size(); i++) {
            listmol.add(listmol.remove(0).generalize());
        }
        /*
         * PHASE2: compare one-by-one
         */
        for (int i = listmol.size() - 1; i > 0; i--) {
            for (int p = i - 1; p >= 0; p--) {
                final ILsaMolecule m1 = listmol.get(i);
                final ILsaMolecule m2 = listmol.get(p);
                if (m2.equals(m1) || m2.moreGenericOf(m1)) {
                    listmol.remove(i);
                    i--;
                } else if (m1.moreGenericOf(m2)) {
                    listmol.remove(p);
                    i--;
                }
            }
        }
    }

    /**
     * @param env
     *            the current environment
     * @param n
     *            the current node
     * @param random
     *            the random engine to use
     * @param timeDist
     *            Time Distribution
     */
    public SAPEREReaction(final Environment<List<? extends ILsaMolecule>> env, final ILsaNode n, final RandomGenerator random, final TimeDistribution<List<? extends ILsaMolecule>> timeDist) {
        super(n, timeDist);
        if (getTimeDistribution() instanceof SAPERETimeDistribution) {
            timedist = (SAPERETimeDistribution) getTimeDistribution();
        } else {
            timedist = null;
        }
        rng = random;
        environment = env;
    }

    @Override
    public Reaction<List<? extends ILsaMolecule>> cloneOnNewNode(final Node<List<? extends ILsaMolecule>> n) {
        final SAPEREReaction res = new SAPEREReaction(environment, (ILsaNode) n, rng, timedist.clone());
        final ArrayList<Condition<List<? extends ILsaMolecule>>> c = new ArrayList<>();
        for (final Condition<List<? extends ILsaMolecule>> cond : getConditions()) {
            c.add(cond.cloneOnNewNode(n));
        }
        final ArrayList<Action<List<? extends ILsaMolecule>>> a = new ArrayList<>();
        for (final Action<List<? extends ILsaMolecule>> act : getActions()) {
            a.add(act.cloneOnNewNode(n, res));
        }
        res.setActions(a);
        res.setConditions(c);
        return res;
    }

    @Override
    public void execute() {
        if (possibleMatches.isEmpty()) {
            for (final ILsaAction a : getActions()) {
                a.setExecutionContext(null, validNodes);
                a.execute();
            }
            return;
        }
        final Position nodePosCache = modifiesOnlyLocally ? environment.getPosition(getNode()) : null;
        final List<? extends ILsaMolecule> localContentCache = modifiesOnlyLocally ? new ArrayList<>(getNode().getLsaSpace()) : null;
        Map<FasterString, ITreeNode<?>> matches = null;
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
        for (final ILsaAction a : getActions()) {
            a.setExecutionContext(matches, validNodes);
            a.execute();
        }

        /*
         * Empty action optimization
         */
        if (modifiesOnlyLocally) {
            final ILsaNode n = getNode();
            if (nodePosCache.equals(environment.getPosition(getNode()))) {
                final List<? extends ILsaMolecule> contents = n.getLsaSpace();
                if (contents.size() == localContentCache.size()) {
                    emptyExecution = localContentCache.containsAll(contents);
                }
            }
        }
    }

    @Override
    @SuppressFBWarnings(value = USM_USELESS_SUBCLASS_METHOD, justification = CAST_NEEDED)
    public List<ILsaAction> getActions() {
        return (List<ILsaAction>) super.getActions();
    }

    @Override
    @SuppressFBWarnings(value = USM_USELESS_SUBCLASS_METHOD, justification = CAST_NEEDED)
    public List<? extends ILsaCondition> getConditions() {
        return (List<? extends ILsaCondition>) super.getConditions();
    }

    /**
     * @return the current environment
     */
    protected Environment<List<? extends ILsaMolecule>> getEnvironment() {
        return environment;
    }

    @Override
    @SuppressFBWarnings(value = USM_USELESS_SUBCLASS_METHOD, justification = CAST_NEEDED)
    public List<ILsaMolecule> getInfluencedMolecules() {
        return (List<ILsaMolecule>) super.getInfluencedMolecules();
    }

    @Override
    @SuppressFBWarnings(value = USM_USELESS_SUBCLASS_METHOD, justification = CAST_NEEDED)
    public List<ILsaMolecule> getInfluencingMolecules() {
        return (List<ILsaMolecule>) super.getInfluencingMolecules();
    }

    @Override
    public ILsaNode getNode() {
        return (ILsaNode) super.getNode();
    }

    /**
     * @return the list of all possible matches
     */
    protected List<Map<FasterString, ITreeNode<?>>> getPossibleMatches() {
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
    protected void updateInternalStatus(final Time curTime, final boolean executed, final Environment<List<? extends ILsaMolecule>> env) {
        if (emptyExecution) {
            emptyExecution = false;
            totalPropensity = 0;
        } else {
            /*
             * Valid nodes must be re-inited, as per issue #
             */
            validNodes = new ArrayList<>((Collection<ILsaNode>) environment.getNeighborhood(getNode()).getNeighbors());
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
                for (final ILsaCondition cond : getConditions()) {
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
                    for (final Map<FasterString, ITreeNode<?>> match : possibleMatches) {
                        timedist.setMatches(match);
                        final double p = timedist.getRate();
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
        return timedist == null || timedist.isStatic();
    }

    @Override
    public double getRate() {
        return totalPropensity;
    }

    @Override
    public String getRateAsString() {
        return numericRate() ? Double.toString(getTimeDistribution().getRate()) : timedist.getRateEquation().toString();
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
    public void setActions(final List<? extends Action<List<? extends ILsaMolecule>>> a) {
        setConditionsAndActions(getConditions(), a);
    }

    @Override
    public void setConditions(final List<? extends Condition<List<? extends ILsaMolecule>>> a) {
        setConditionsAndActions(a, getActions());
    }

    private void setConditionsAndActions(final List<? extends Condition<List<? extends ILsaMolecule>>> c, final List<? extends Action<List<? extends ILsaMolecule>>> a) {
        super.setConditions(c);
        super.setActions(a);
        modifiesOnlyLocally = getOutputContext() == Context.LOCAL;
        /*
         * The following optimization only makes sense if the reaction acts
         * locally. Otherwise there is no control on where the modified
         * molecules will end up.
         */
        final List<ILsaMolecule> influencing = new ArrayList<>(getInfluencingMolecules());
        final List<ILsaMolecule> influenced = new ArrayList<>(getInfluencedMolecules());
        if (getInputContext() == Context.LOCAL && modifiesOnlyLocally) {
            /*
             * Moreover, since there is no control over the personalised agents,
             * it's required to check that all the actions are the standard
             * manipulations.
             */
            boolean allStandard = true;
            for (final ILsaAction act : getActions()) {
                if (!(act instanceof LsaStandardAction)) {
                    allStandard = false;
                    break;
                }
            }
            if (allStandard) {
                for (final ILsaMolecule m : influencing) {
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
                    if (influenced.contains(m)) {
                        influenced.remove(m);
                    } else {
                        influenced.add(m);
                    }
                }
            }
        }
        screen(influencing);
        screen(influenced);
        setInfluencingMolecules(influencing);
        setInfluencedMolecules(influenced);
    }

    /**
     * @param pm
     *            the list of all possible matches
     */
    protected void setPossibleMatches(final List<Map<FasterString, ITreeNode<?>>> pm) {
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
