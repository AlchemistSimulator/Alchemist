/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * 
 * 
 * @param <T>
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
    @Override
    public ChemicalReaction<T> cloneOnNewNode(final Node<T> n, final Time currentTime) {
        return makeClone(() -> new ChemicalReaction<>(n, getTimeDistribution().clone(currentTime)));
    }

    @Override
    public final void initializationComplete(final Time t, final Environment<T, ?> env) {
        update(t, true, env);
    }

    /**
     * Subclasses must call super.updateInternalStatus for the rate to get updated in case of method override.
     */
    @Override
    protected void updateInternalStatus(final Time curTime, final boolean executed, final Environment<T, ?> env) {
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
