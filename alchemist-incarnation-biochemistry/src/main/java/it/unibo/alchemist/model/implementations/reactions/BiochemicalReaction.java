/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.actions.AbstractNeighborAction;
import it.unibo.alchemist.model.implementations.conditions.AbstractNeighborCondition;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/** 
 * A biochemical Reaction.
 */
public final class BiochemicalReaction extends ChemicalReaction<Double> {

    private static final long serialVersionUID = 3849210665619933894L;
    private final Environment<Double, ?> environment;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All subclasses are actually serializable")
    private final RandomGenerator random;
    private Map<Node<Double>, Double> validNeighbors = new LinkedHashMap<>();
    /*
     * Check if at least a neighbor condition is present in the reaction.
     * It is used when a neighbor action is present:
     * - If a neighbor condition AND a neighbor action are present, the target node for the action must be 
     *   calculated.
     * - If only neighbor actions are present the target node must be randomly choose.
     */
    private boolean neighborConditionsPresent;

    /**
     * @param node
     *            node
     * @param timeDistribution
     *            time distribution
     * @param environment
     *            the environment
     * @param randomGenerator
     *            the random generator
     */
    public BiochemicalReaction(
            final Node<Double> node,
            final TimeDistribution<Double> timeDistribution,
            final Environment<Double, ?> environment,
            final RandomGenerator randomGenerator
    ) {
        super(node, timeDistribution);
        this.environment = environment;
        random = randomGenerator;
    }

    @Override
    public BiochemicalReaction cloneOnNewNode(final Node<Double> node, final Time currentTime) {
        return new BiochemicalReaction(node, getTimeDistribution().cloneOnNewNode(node, currentTime), environment, random);
    }

    @Override 
    protected void updateInternalStatus(
            final Time currentTime,
            final boolean hasBeenExecuted,
            final Environment<Double, ?> environment
    ) {
        if (neighborConditionsPresent) {
            validNeighbors = getConditions().stream()
                .filter(it -> it instanceof AbstractNeighborCondition)
                .map(it -> (AbstractNeighborCondition<Double>) it)
                .map(AbstractNeighborCondition::getValidNeighbors)
                .reduce((m1, m2) -> m1.entrySet().stream()
                        .map(it -> new Container(it.getKey(), it.getValue(), m2.get(it.getKey())))
                        .filter(it -> it.propensity2 != null)
                        .collect(toMap(e -> e.node, e -> e.propensity1 * e.propensity2)))
                .orElseThrow(() -> new IllegalStateException(
                        "At least a neighbor condition is present, but the mapping was empty"
                ));
        }
        super.updateInternalStatus(currentTime, hasBeenExecuted, environment);
    }

    @Override 
    public void execute() {
        if (neighborConditionsPresent) {
            final List<Pair<Node<Double>, Double>> neighborsList = validNeighbors.entrySet().stream()
                    .map(e -> new Pair<>(e.getKey(), e.getValue()))
                    .collect(toList());
            final Optional<Node<Double>> target = Optional.of(neighborsList)
                    .filter(it -> !it.isEmpty())
                    .map(it -> new EnumeratedDistribution<>(random, it))
                    .map(EnumeratedDistribution::sample);
            getActions().forEach(action -> {
                if (action instanceof AbstractNeighborAction) {
                    target.ifPresent(((AbstractNeighborAction<Double>) action)::execute);
                } else {
                    action.execute();
                }
            });
        } else {
            super.execute();
        }
    }

    @Override
    public void setConditions(final List<? extends Condition<Double>> conditions) {
        super.setConditions(conditions);
        neighborConditionsPresent = conditions.stream().anyMatch(it -> it instanceof AbstractNeighborCondition);
    }

    private static final class Container {
        private final Node<Double> node;
        private final Double propensity1;
        private final Double propensity2;
        private Container(final Node<Double> node, final Double propensity1, final Double propensity2) {
            this.node = node;
            this.propensity1 = propensity1;
            this.propensity2 = propensity2;
        }
    }
}
