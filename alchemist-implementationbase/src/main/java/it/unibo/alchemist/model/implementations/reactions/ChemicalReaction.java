/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Time;
import it.unibo.alchemist.model.TimeDistribution;

import javax.annotation.Nonnull;

/**
 * 
 * 
 * @param <T> concentration type
 */
public class ChemicalReaction<T> extends AbstractReaction<T> {

    private static final long serialVersionUID = -5260452049415003046L;
    private double currentRate;

    /**
     * @param n
     *            node
     * @param pd
     *            time distribution
     */
    public ChemicalReaction(final Node<T> n, final TimeDistribution<T> pd) {
        super(n, pd);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ChemicalReaction<T> cloneOnNewNode(@Nonnull final Node<T> node, @Nonnull final Time currentTime) {
        return makeClone(() -> new ChemicalReaction<>(node, getTimeDistribution().cloneOnNewNode(node, currentTime)));
    }

    @Override
    public final void initializationComplete(@Nonnull final Time atTime, @Nonnull final Environment<T, ?> environment) {
        update(atTime, true, environment);
    }

    /**
     * Subclasses must call super.updateInternalStatus for the rate to get updated in case of method override.
     */
    @Override
    protected void updateInternalStatus(
            final Time currentTime,
            final boolean hasBeenExecuted,
            final Environment<T, ?> environment
    ) {
        currentRate = getTimeDistribution().getRate();
        for (final Condition<T> cond : getConditions()) {
            final double v = cond.getPropensityContribution();
            if (v == 0) {
                currentRate = 0;
                break;
            }
            if (v < 0) {
                throw new IllegalStateException("Condition " + cond + " returned a negative propensity conditioning value");
            }
            currentRate *= cond.getPropensityContribution();
        }
    }

    @Override
    public final double getRate() {
        return currentRate;
    }

}
