/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors


 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */


package it.unibo.alchemist.model.implementations.reactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import it.unibo.alchemist.model.implementations.actions.AbstractNeighborAction;
import it.unibo.alchemist.model.implementations.conditions.AbstractNeighborCondition;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;


/** 
 * Unused class (probably it will be removed).
 */
public class BiochemicalReaction extends ChemicalReaction<Double> {

    private static final long serialVersionUID = 3849210665619933894L;
    private Map<Node<Double>, Double> validNeighbors = new HashMap<>(0);
    private final Node<Double> node;
    private final Environment<Double> environment;
    /*
     * Check if at least a neghbor condition is present in the reaction.
     * It is used when a neighbor action is present:
     * - If a neighbor condition AND a neighbor action are present, the target node for the action must be 
     *   calculated.
     * - If only neighbor actions are present the target node must be randomly choose.
     */
    private boolean neighborConditionsPresent;
    private boolean neighborActionsPresent;

    private static Map<Node<Double>, Double> intersectMap(final Map<Node<Double>, Double> map1, final Map<Node<Double>, Double> map2) {
        final Map<Node<Double>, Double> ret = new HashMap<>();
        for (final Node<Double> n : map1.keySet()) {
            if (map2.containsKey(n)) {
                ret.put(n, map1.get(n) + map2.get(n)); // why addition? search in some papers. TODO
            }
        }
        return ret;
    }

    /**
     * @param n
     *            node
     * @param td
     *            time distribution
     * @param env 
     *            the environment
     */
    public BiochemicalReaction(final Node<Double> n, final TimeDistribution<Double> td, final Environment<Double> env) {
        super(n, td);
        node = n;
        environment = env;
    }

    @Override
    public BiochemicalReaction cloneOnNewNode(final Node<Double> node) {
        return new BiochemicalReaction(node, getTimeDistribution(), environment);
    }

    @Override 
    protected void updateInternalStatus(final Time curTime, final boolean executed, final Environment<Double> env) {
        if (neighborConditionsPresent) {
            validNeighbors.clear();
            validNeighbors = env.getNeighborhood(node).getNeighbors().stream().collect(Collectors.<Node<Double>, Node<Double>, Double>toMap(
                    n -> n,
                    n -> 0d));
            for (final Condition<Double> cond : getConditions()) {
                if (cond instanceof AbstractNeighborCondition) {
                    validNeighbors = intersectMap(validNeighbors, ((AbstractNeighborCondition<Double>) cond).getValidNeighbors(validNeighbors.keySet()));
                    if (validNeighbors.isEmpty()) { // maybe speedup the check
                        break;
                    }
                }
            }
        }
        super.updateInternalStatus(curTime, executed, env);
    }

    @Override 
    public void execute() {
        if (neighborConditionsPresent) {
            final Optional<Map.Entry<Node<Double>, Double>> neighTarget = validNeighbors.entrySet().stream().max((e1, e2) -> e1.getValue().compareTo(e2.getValue()));
            for (final Action<Double> a : getActions()) {
                if (a instanceof AbstractNeighborAction && neighTarget.isPresent()) {
                    ((AbstractNeighborAction<Double>) a).execute(neighTarget.get().getKey());
                } else {
                    a.execute();
                }
            }
        } else {
            super.execute();
        }
    }

    @Override
    public boolean canExecute() {
        if (neighborActionsPresent || neighborConditionsPresent) { // check if the neighborhood is actually present. It can be not present due to node movement or node deleting
            final Neighborhood<Double> currentNeigh = environment.getNeighborhood(node);
            validNeighbors.entrySet().stream().filter(e -> currentNeigh.contains(e.getKey())).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            if (validNeighbors.isEmpty()) {
                return false;
            }
        }
        return super.canExecute();
    }

    @Override
    public void setConditions(final List<? extends Condition<Double>> c) {
        super.setConditions(c);
        neighborConditionsPresent = getInputContext().equals(Context.NEIGHBORHOOD);
    }

    @Override
    public void setActions(final List<? extends Action<Double>> c) {
        super.setActions(c);
        neighborActionsPresent = getOutputContext().equals(Context.NEIGHBORHOOD);
    }

}
