/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.movestrategies.speed;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * This strategy slows down nodes depending on how many "interacting" nodes are
 * found in the surroundings. It is an attempt at modeling crowding slow-downs.
 *
 * @param <P> position type
 * @param <T> concentration type
 */
public final class InteractWithOthers<T, P extends Position<? extends P>> implements SpeedSelectionStrategy<T, P> {

    private static final long serialVersionUID = -1900168887685703120L;
    private static final double MINIMUM_DISTANCE_WALKED = 1;
    private final Environment<T, P> environment;
    private final Node<T> node;
    private final Molecule interactingMolecule;
    private final double radius, interaction, speed;

    /**
     * @param environment
     *            the environment
     * @param n
     *            the node
     * @param reaction
     *            the reaction
     * @param interactingMolecule
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
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public InteractWithOthers(
        final Environment<T, P> environment,
        final Node<T> n,
        final Reaction<T> reaction,
        final Molecule interactingMolecule,
        final double speed,
        final double radius,
        final double interaction
    ) {
        this.environment = Objects.requireNonNull(environment);
        node = Objects.requireNonNull(n);
        this.interactingMolecule = Objects.requireNonNull(interactingMolecule);
        if (radius < 0) {
            throw new IllegalArgumentException("The radius must be positive (provided: " + radius + ")");
        }
        this.radius = radius;
        this.speed = speed / reaction.getRate();
        this.interaction = interaction;
    }

    @Override
    public double getNodeMovementLength(final P target) {
        double crowd = 0;
        final Collection<? extends Node<T>> neighs = radius > 0
            ? environment.getNodesWithinRange(node, radius)
            : Collections.emptyList();
        if (neighs.size() > 1 / interaction) {
            for (final Node<T> neigh : neighs) {
                if (neigh.contains(interactingMolecule)) {
                    crowd += 1 / environment.getDistanceBetweenNodes(node, neigh);
                }
            }
        }
        return Math.max(speed / (crowd * interaction + 1), MINIMUM_DISTANCE_WALKED);
    }

    @Override
    public SpeedSelectionStrategy<T, P> cloneIfNeeded(final Node<T> destination, final Reaction<T> reaction) {
        return new InteractWithOthers<>(environment, destination, reaction, interactingMolecule, speed, radius, interaction);
    }
}
