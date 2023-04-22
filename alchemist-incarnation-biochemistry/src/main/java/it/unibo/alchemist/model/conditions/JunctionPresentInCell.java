/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CellProperty;

import java.util.Collections;
import java.util.Objects;

/**
 */
public final class JunctionPresentInCell extends AbstractNeighborCondition<Double> {

    private static final long serialVersionUID = 4213307452790768059L;

    private final Junction junction;
    private final Environment<Double, ?> environment;
    private final CellProperty<?> cell;

    /**
     * 
     * @param junction the junction
     * @param node the node
     * @param environment the environment
     */
    public JunctionPresentInCell(final Environment<Double, ?> environment, final Node<Double> node, final Junction junction) {
        super(environment, node);
        cell = node.asPropertyOrNull(CellProperty.class);
        Objects.requireNonNull(
                cell,
                "This Condition can be set only in node with " + CellProperty.class.getSimpleName()
        );
        declareDependencyOn(junction);
        this.junction = junction;
        this.environment = environment;
    }

    @Override
    public boolean isValid() {
        return cell.containsJunction(junction);
    }

    @Override
    public JunctionPresentInCell cloneCondition(final Node<Double> node, final Reaction<Double> r) {
        return new JunctionPresentInCell(environment, node, junction);
    }

    @Override
    protected double getNeighborPropensity(final Node<Double> neighbor) {
        // the neighbor's propensity is computed as the number of junctions it has
        return cell.getJunctions()
                .getOrDefault(junction, Collections.emptyMap())
                .getOrDefault(neighbor, 0);
    }

    @Override
    public String toString() {
        return "junction " +  junction.toString() + " present ";
    }

}
