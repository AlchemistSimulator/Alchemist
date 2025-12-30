/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions;

import com.google.common.base.Optional;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;

import javax.annotation.Nonnull;
import java.io.Serial;
import java.util.Objects;

/**
 * A condition that holds true only if the tracked {@link Molecule} changed its
 * {@link it.unibo.alchemist.model.Concentration}.
 *
 * @param <T> concentration type
 */
public final class ConcentrationChanged<T> extends AbstractCondition<T> {

    @Serial
    private static final long serialVersionUID = 1L;
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
        declareDependencyOn(target);

        addObservableDependency(node.observeConcentration(target));

        validity = node.observeConcentration(target).map(it -> {
            if (!hasFlipped) {
                final var maybeValue = Optional.fromNullable(it.getOrNull());
                if (!maybeValue.equals(previous)) {
                    hasFlipped = true;
                    previous = maybeValue;
                }
            }
            return hasFlipped;
        });

        propensity = validity.map(valid -> valid ? 1d : 0d);
    }

    @Override
    public ConcentrationChanged<T> cloneCondition(final Node<T> newNode, final Reaction<T> newReaction) {
        return new ConcentrationChanged<>(newNode, target);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public boolean isValid() {
        final boolean flip = hasFlipped;
        hasFlipped = false;
        return flip;
    }

    @Override
    public String toString() {
        return target + "changes value";
    }

}
