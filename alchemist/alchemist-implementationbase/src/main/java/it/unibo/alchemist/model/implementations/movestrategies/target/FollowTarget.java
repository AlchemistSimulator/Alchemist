/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies.target;

import static org.danilopianini.util.regex.Patterns.FLOAT_PATTERN;

import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Matcher;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;

/**
 * This strategy reads the value of a "target" molecule and tries to interpret
 * it as a coordinate.
 * 
 * @param <T>
 *            Concentration type
 */
public class FollowTarget<T, P extends Position<P>> implements TargetSelectionStrategy<P> {

    private static final long serialVersionUID = -446053307821810437L;
    private final Environment<T, P> environment;
    private final Node<T> node;
    private final Molecule track;

    /**
     * @param env
     *            the environment
     * @param n
     *            the node
     * @param targetMolecule
     *            the target molecule
     */
    public FollowTarget(final Environment<T, P> env, final Node<T> n, final Molecule targetMolecule) {
        environment = env;
        node = n;
        track = targetMolecule;
    }

    /**
     * @param x
     *            first coordinate extracted from the target concentration
     * @param y
     *            second coordinate extracted from the target concentration
     * @return a {@link Position} built using such parameters
     */
    protected P createPosition(final double x, final double y) {
        return environment.makePosition(x, y);
    }

    /**
     * @return the current position
     */
    protected final P getCurrentPosition() {
        return environment.getPosition(node);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final P getTarget() {
        final Optional<T> optt = Optional.ofNullable(node.getConcentration(track));
        if (optt.isPresent()) {
            final T conc = optt.get();
            if (conc instanceof Position) {
                return (P) conc;
            }
            double x = Double.NaN;
            double y = Double.NaN;
            if (conc instanceof Iterable) {
                final Iterator<?> iterator = ((Iterable<?>) conc).iterator();
                while (iterator.hasNext() && Double.isNaN(y)) {
                    final Object elem = iterator.next();
                    double val;
                    if (elem instanceof Number) {
                        val = ((Number) elem).doubleValue();
                    } else if (elem == null) {
                        return getCurrentPosition();
                    } else {
                        try {
                            val = Double.parseDouble(elem.toString());
                        } catch (NumberFormatException e) {
                            return getCurrentPosition();
                        }
                    }
                    if (Double.isNaN(x)) {
                        x = val;
                    } else {
                        y = val;
                    }
                }
            } else {
                final Matcher m = FLOAT_PATTERN
                        .matcher(conc instanceof CharSequence ? (CharSequence) conc : conc.toString());
                while (Double.isNaN(y) && m.find()) {
                    final String val = m.group();
                    /*
                     * It can not fail, unless the RegexUtil utility is broken
                     */
                    if (Double.isNaN(x)) {
                        x = Double.parseDouble(val);
                    } else {
                        y = Double.parseDouble(val);
                    }
                }
            }
            if (!Double.isNaN(y)) {
                return createPosition(x, y);
            }
        }
        return getCurrentPosition();
    }
}
