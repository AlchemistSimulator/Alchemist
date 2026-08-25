/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.reactions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.TimeDistribution;
import it.unibo.alchemist.model.reactions.AbstractNodeReaction;
import it.unibo.alchemist.model.sapere.ILsaAction;
import it.unibo.alchemist.model.sapere.ILsaCondition;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode;
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule;
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime;
import it.unibo.alchemist.model.sapere.timedistributions.SAPERETimeDistribution;
import it.unibo.alchemist.model.timedistributions.AbstractDistribution;
import it.unibo.alchemist.model.timedistributions.ExponentialTime;
import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.lang.HashString;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class realizes a reaction with Lsa concentrations.
 *
 */
public final class SAPERENodeReaction extends AbstractNodeReaction<List<ILsaMolecule>> {

    private final Environment<List<ILsaMolecule>, ?> environment;
    @SuppressFBWarnings(
            value = "SE_BAD_FIELD",
            justification = "All provided RandomGenerator implementations are actually Serializable"
    )
    private final RandomGenerator rng;
    private final SAPERETimeDistribution timeDistribution;

    private List<Map<HashString, ITreeNode<?>>> possibleMatches = new ArrayList<>(0);
    private List<Map<ILsaNode, List<ILsaMolecule>>> possibleRemove = new ArrayList<>(0);
    private List<Double> propensities = new ArrayList<>(0);
    private double totalPropensity;
    private List<ILsaNode> validNodes = new ArrayList<>(0);

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
    public SAPERENodeReaction(
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

    @Nonnull
    @Override
    public NodeReaction<List<ILsaMolecule>> cloneOnNewNode(
        @Nonnull final Node<List<ILsaMolecule>> node,
        @Nonnull final Time currentTime
    ) {
        return prepareClone(
            new SAPERENodeReaction(
                environment,
                (ILsaNode) node,
                rng,
                getTimeDistribution().newInstanceOn(node)
            ),
            currentTime
        );
    }

    /**
     * @return the inner {@link Action} list, cast
     */
    @SuppressWarnings("unchecked")
    private List<ILsaAction> getSAPEREActions() {
        return (List<ILsaAction>) (List<? extends Action<List<ILsaMolecule>>>) getActions();
    }

    /**
     * @return the inner {@link Condition} list, cast
     */
    @SuppressWarnings("unchecked")
    private List<ILsaCondition> getSAPEREConditions() {
        return (List<ILsaCondition>) (List<? extends Condition<List<ILsaMolecule>>>) getConditions();
    }

    @Override
    public void execute() {
        if (possibleMatches.isEmpty()) {
            executeActions(null);
            return;
        }
        final int selectedMatchIndex = selectMatchIndex();
        final Map<HashString, ITreeNode<?>> matches = possibleMatches.get(selectedMatchIndex);
        /*
         * The matched LSAs must be removed from the local space, if no action
         * added them back.
         */
        removeMatchedMolecules(possibleRemove.get(selectedMatchIndex));
        /*
         * #T Must be loaded by the reaction, which is the only structure aware
         * of the time. Other special values (#NEIG, #O, #D) will be allocated
         * inside the actions.
         */
        matches.put(LsaMolecule.SYN_T, new NumTreeNode(getNextOccurrence().getCurrent().toDouble()));
        executeActions(matches);
    }

    private void executeActions(final Map<HashString, ITreeNode<?>> matches) {
        for (final ILsaAction action : getSAPEREActions()) {
            action.setExecutionContext(matches, validNodes);
            action.execute();
        }
    }

    private void removeMatchedMolecules(final Map<ILsaNode, List<ILsaMolecule>> toRemove) {
        for (final Entry<ILsaNode, List<ILsaMolecule>> entry : toRemove.entrySet()) {
            final ILsaNode node = entry.getKey();
            for (final ILsaMolecule molecule : entry.getValue()) {
                node.removeConcentration(molecule);
            }
        }
    }

    private int selectMatchIndex() {
        /*
         * If there is infinite propensity, the last match added is the one to
         * choose, since it is the one which generated the "infinity" value.
         */
        if (totalPropensity == Double.POSITIVE_INFINITY) {
            return possibleMatches.size() - 1;
        }
        /*
         * If the rate is numeric, the choice is just random
         */
        if (numericRate()) {
            return rng.nextInt(possibleMatches.size());
        }
        /*
         * Otherwise, the matches must be chosen randomly using their
         * propensities
         */
        return selectWeightedMatchIndex();
    }

    private int selectWeightedMatchIndex() {
        final double selectedPropensity = rng.nextDouble() * totalPropensity;
        double cumulativePropensity = 0;
        for (int i = 0; i < propensities.size(); i++) {
            cumulativePropensity += propensities.get(i);
            if (cumulativePropensity > selectedPropensity) {
                return i;
            }
        }
        // Floating-point rounding can cause selectedPropensity == totalPropensity;
        // fall back to the last bucket in that case.
        return propensities.size() - 1;
    }

    @Override
    protected void onInitializationComplete(
        @Nonnull final Time atTime,
        @Nonnull final Environment<List<ILsaMolecule>, ?> currentEnvironment
    ) {
        if (!isNewlyInstantiatedProgram()) {
            refreshReactionState(atTime, currentEnvironment);
            initializeNewProgramScheduling(atTime);
        }
    }

    @Override
    protected void refreshReactionState(
        @Nonnull final Time currentTime,
        @Nonnull final Environment<List<ILsaMolecule>, ?> currentEnvironment
    ) {
        /*
         * Valid nodes must be re-initialized, as per issue #
         */
        final Collection<? extends Node<List<ILsaMolecule>>> neighs =
                this.environment.getNeighborhood(getNode()).getCurrent().getNeighbors();
        validNodes = new ArrayList<>(neighs.size());
        for (final Node<List<ILsaMolecule>> neigh: neighs) {
            validNodes.add((ILsaNode) neigh);
        }
        if (getConditions().isEmpty()) {
            totalPropensity = baseRate();
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
                totalPropensity = possibleMatches.size() * baseRate();
            } else {
                /*
                 * For each possible match, compute the propensity
                 */
                for (final Map<HashString, ITreeNode<?>> match : possibleMatches) {
                    timeDistribution.setMatches(match);
                    final double p = timeDistribution.getRate();
                    if (Double.isNaN(p) || p < 0d) {
                        throw new IllegalStateException("Invalid SAPERE propensity for match: " + p);
                    }
                    propensities.add(p);
                    totalPropensity += p;
                    if (totalPropensity == Double.POSITIVE_INFINITY) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected void scheduleNextOccurrenceAfterFiring(@Nonnull final Time currentTime) {
        scheduleFreshOccurrence(currentTime);
    }

    @Override
    protected void updateSchedulingAfterInvalidation(@Nonnull final Time currentTime) {
        scheduleFreshOccurrence(currentTime);
    }

    private void scheduleFreshOccurrence(@Nonnull final Time currentTime) {
        final double totalRate = getRate();
        if (totalRate == 0d) {
            setNextOccurrence(Time.INFINITY);
            return;
        }
        if (Double.isNaN(totalRate) || totalRate < 0) {
            throw new IllegalStateException("Invalid SAPERE propensity: total=" + totalRate);
        }
        /*
         * SAPERE exponential distributions evaluate their rate against the
         * currently installed match.  Refreshing the reaction leaves the last
         * match installed, which may have zero propensity even when the total
         * propensity is positive.  Install a match with a positive propensity
         * before drawing, otherwise sampling would produce infinity and the
         * subsequent zero scaling would yield NaN.
         */
        if (getTimeDistribution() instanceof SAPEREExponentialTime && !numericRate()) {
            final int positiveMatch = findPositiveMatch();
            timeDistribution.setMatches(possibleMatches.get(positiveMatch));
        }
        final double generatorRate = getTimeDistribution() instanceof SAPEREExponentialTime
            ? ((SAPEREExponentialTime) getTimeDistribution()).getRate()
            : getTimeDistribution() instanceof ExponentialTime
                ? ((ExponentialTime<?>) getTimeDistribution()).getLambda()
            : totalRate;
        if (Double.isNaN(generatorRate) || generatorRate < 0) {
            throw new IllegalStateException("Invalid SAPERE propensity: generator=" + generatorRate + ", total=" + totalRate);
        }
        final double scaling = generatorRate == totalRate
            || Double.isInfinite(generatorRate) && Double.isInfinite(totalRate) ? 1d : generatorRate / totalRate;
        final Time sample = validatedSample();
        final Time schedulingTime = getTimeDistribution() instanceof AbstractDistribution
            ? currentTime.compareTo(((AbstractDistribution<?>) getTimeDistribution()).getStartTime()) < 0
                ? ((AbstractDistribution<?>) getTimeDistribution()).getStartTime()
                : currentTime
            : currentTime;
        final Time delay = sample.times(scaling);
        if (!delay.isFinite() || delay.compareTo(Time.ZERO) < 0) {
            throw new IllegalStateException("Invalid transformed SAPERE delay: " + delay);
        }
        setNextOccurrence(schedulingTime.plus(delay));
    }

    private int findPositiveMatch() {
        for (int i = 0; i < propensities.size(); i++) {
            if (propensities.get(i) == Double.POSITIVE_INFINITY) {
                return i;
            }
        }
        for (int i = 0; i < propensities.size(); i++) {
            if (propensities.get(i) > 0) {
                return i;
            }
        }
        throw new IllegalStateException("Positive SAPERE propensity without a positive match");
    }

    private boolean numericRate() {
        return timeDistribution == null || timeDistribution.isStatic();
    }

    private double baseRate() {
        return timeDistribution == null ? super.getRate() : timeDistribution.getRate();
    }

    @Override
    public double getRate() {
        return totalPropensity;
    }

    @Override
    @Nonnull
    public String getRateAsString() {
        return numericRate() ? Double.toString(baseRate()) : timeDistribution.getRateEquation().toString();
    }

}
