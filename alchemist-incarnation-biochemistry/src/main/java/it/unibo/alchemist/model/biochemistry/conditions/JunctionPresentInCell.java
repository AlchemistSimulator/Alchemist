/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.biochemistry.CellProperty;
import it.unibo.alchemist.model.biochemistry.molecules.Junction;
import it.unibo.alchemist.model.observation.MutableObservable;
import it.unibo.alchemist.model.observation.Observable;
import it.unibo.alchemist.model.observation.ObservableExtensions;

import java.io.Serial;
import java.util.Objects;

/**
 * Condition that is valid if a specific junction is present in the cell.
 */
public final class JunctionPresentInCell extends AbstractNeighborCondition<Double> {

    @Serial
    private static final long serialVersionUID = 4213307452790768059L;

    private final Junction junction;
    private final Environment<Double, ?> environment;
    private final CellProperty<?> cell;

    /**
     * @param junction    the junction
     * @param node        the node
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
        setUpObservability();
    }

    @Override
    public JunctionPresentInCell cloneCondition(final Node<Double> newNode, final Reaction<Double> newReaction) {
        return new JunctionPresentInCell(environment, newNode, junction);
    }

    @Override
    protected Observable<Double> observeNeighborPropensity(final Node<Double> neighbor) {
        return ObservableExtensions.INSTANCE.switchMap(
            cell.getJunctions().get(junction),
            maybeJunctions -> {
                if (maybeJunctions.isNone() || maybeJunctions.getOrNull().isEmpty()) {
                    return MutableObservable.Companion.observe(0.0);
                }
                return maybeJunctions.getOrNull().get(neighbor)
                    .map(maybeCount -> maybeCount.fold(() -> 0d, Integer::doubleValue));
            }
        );
    }

    @Override
    public String toString() {
        return "junction " + junction.toString() + " present ";
    }

    private void setUpObservability() {
        setValidity(cell.observeContainsJunction(junction));
    }
}
