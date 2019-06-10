/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies.speed;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * This strategy slows down nodes depending on how many "interacting" nodes are
 * found in the surroundings. It is an attempt at modeling crowding slow-downs.
 *
 * @param <P> position type
 * @param <T>
 */
public final class InteractWithOthers<T, P extends Position<? extends P>> implements SpeedSelectionStrategy<P> {

    private static final long serialVersionUID = -1900168887685703120L;
    private static final double MINIMUM_DISTANCE_WALKED = 1;
    private final Environment<T, P> env;
    private final Node<T> node;
    private final Molecule interacting;
    private final double rd, in, sp;

    /**
     * @param environment
     *            the environment
     * @param n
     *            the node
     * @param reaction
     *            the reaction
     * @param inter
     *            the molecule that identifies an interacting node
     * @param speed
     *            the normal speed of the node
     * @param radius
     *            the radius where to search for interacting nodes
     * @param interaction
     *            the interaction factor. This will be multiplied by a crowd
     *            factor dynamically computed, and the speed will be divided by
     *            the number obtained
     */
    public InteractWithOthers(final Environment<T, P> environment, final Node<T> n, final Reaction<T> reaction,
            final Molecule inter, final double speed, final double radius, final double interaction) {
        env = Objects.requireNonNull(environment);
        node = Objects.requireNonNull(n);
        interacting = Objects.requireNonNull(inter);
        if (radius < 0) {
            throw new IllegalArgumentException("The radius must be positive (provided: " + radius + ")");
        }
        rd = radius;
        sp = speed / reaction.getRate();
        in = interaction;

    }

    @Override
    public double getNodeMovementLength(final P target) {
        double crowd = 0;
        final Collection<? extends Node<T>> neighs = rd > 0 ? env.getNodesWithinRange(node, rd) : Collections.emptyList();
        if (neighs.size() > 1 / in) {
            for (final Node<T> neigh : neighs) {
                if (neigh.contains(interacting)) {
                    crowd += 1 / env.getDistanceBetweenNodes(node, neigh);
                }
            }
        }
        return Math.max(sp / (crowd * in + 1), MINIMUM_DISTANCE_WALKED);
    }

}
