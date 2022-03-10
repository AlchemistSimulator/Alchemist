/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CellProperty;
import it.unibo.alchemist.model.interfaces.properties.CircularCellProperty;

import java.util.Objects;

/**
 * 
 * @param <P> {@link Position} type
 */
public final class CellMove<P extends Position<P>> extends AbstractMoveNode<Double, P> {

    private static final long serialVersionUID = 1L;
    private final boolean inPercent;
    private final double delta;
    private final CellProperty<P> cell;

    /**
     * Initialize an Action that move the cell of a given space delta, which can be expressed in percent of the cell's
     * diameter or in absolute.
     * If the cell has diameter 0, the only way to express delta is absolute.
     * There's no way to decide the direction of the cell by this {@link it.unibo.alchemist.model.interfaces.Action}.
     * This is inferred by the polarization vector contained in the cell.
     * 
     * @param environment the {@link Environment}
     * @param node the {@link Node} in which the {@link it.unibo.alchemist.model.interfaces.Action} is contained.
     *             This can be only a CellNode.
     * @param inPercent a boolean parameter which set the way of expressing delta: if is true the cell movement will be
     *                  (delta * cellDiameter), otherwise will be simply delta. If cellDiameter is zero, this
     *                  {@link it.unibo.alchemist.model.interfaces.Action} will in both cases behave like
     *                  inPercent == false.
     * @param delta the distance at which the cell will be moved.
     */
    public CellMove(
            final Environment<Double, P> environment,
            final Node<Double> node,
            final boolean inPercent,
            final double delta
    ) {
        super(environment, node);
        this.inPercent = inPercent;
        cell = Objects.requireNonNull(
                node.asPropertyOrNull(CellProperty.class),
                "CellMove can be setted only in cells."
        );
        if (inPercent) {
            if (cell instanceof CircularCellProperty && ((CircularCellProperty<P>) cell).getRadius() != 0) {
                this.delta = ((CircularCellProperty<P>) cell).getDiameter() * delta;
            } else {
                throw new IllegalArgumentException(
                        "Can't set distance in percent of the cell's diameter if cell has not a diameter"
                );
            }
        } else {
            this.delta = delta;
        }
    }

    @Override
    public CellMove<P> cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new CellMove<>(getEnvironment(), node, inPercent, delta);
    }

    @Override
    public P getNextPosition() {
        return getEnvironment().makePosition(
                delta * cell.getPolarizationVersor().getCoordinate(0),
                delta * cell.getPolarizationVersor().getCoordinate(1)
        );
    }

    @Override
    public void execute() {
        super.execute();
        cell.setPolarizationVersor(getEnvironment().makePosition(0, 0));
    }
}
