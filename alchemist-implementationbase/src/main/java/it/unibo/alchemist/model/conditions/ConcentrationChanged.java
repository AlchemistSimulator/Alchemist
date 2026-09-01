/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions;

import com.google.common.base.Optional;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A condition that holds true only if the tracked {@link Molecule} changed its
 * {@link it.unibo.alchemist.model.Concentration}.
 *
 * @param <T> concentration type
 */
public final class ConcentrationChanged<T> extends AbstractCondition<T> {

    private final Molecule target;
    @Nonnull
    private Optional<T> previous;
    private boolean hasFlipped;

    /**
     * @param node the node
     * @param target the molecule whose concentration may change
     */
    public ConcentrationChanged(@Nonnull final Node<T> node, @Nonnull final Molecule target) {
        super(node);
        this.target = Objects.requireNonNull(target);
        previous = Optional.fromNullable(node.getConcentration(target));
        hasFlipped = false;
        addObservableDependency(node.observeConcentration(target));
        setValidity(node.observeConcentration(target).map(it -> {
            if (!hasFlipped) {
                final var maybeValue = Optional.fromNullable(it.getOrNull());
                if (!maybeValue.equals(previous)) {
                    hasFlipped = true;
                    previous = maybeValue;
                }
            }
            return hasFlipped;
        }));

    }

    @Nonnull
    @Override
    public ConcentrationChanged<T> cloneCondition(@Nonnull final Node<T> newNode, @Nonnull final NodeReaction<T> newReaction) {
        return new ConcentrationChanged<>(newNode, target);
    }

    @Override
    public void reactionReady() {
        hasFlipped = false;
    }

    @Nonnull
    @Override
    public String toString() {
        return target + "changes value";
    }

}
