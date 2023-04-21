/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Dependency;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.EuclideanEnvironment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;

/**
 * This action moves a node inside a given environment.
 * 
 * @param <T> concentration type
 * @param <P> position type
 */
public abstract class AbstractMoveNode<T, P extends Position<P>> extends AbstractAction<T> {

    private static final long serialVersionUID = -5867654295577425307L;
    private final Environment<T, P> environment;
    private final boolean absolute;

    /**
     * Builds a new move node action. By default the movements are relative.
     * 
     * @param environment
     *            The environment where to move
     * @param node
     *            The node to which this action belongs
     */
    protected AbstractMoveNode(final Environment<T, P> environment, final Node<T> node) {
        this(environment, node, false);
    }

    /**
     * @param environment
     *            The environment where to move
     * @param node
     *            The node to which this action belongs
     * @param isAbsolute
     *            if set to true, the environment expects the movement to be
     *            expressed in absolute coordinates. It means that, if a node in
     *            (1,1) wants to move to (2,3), its getNextPosition() must
     *            return (2,3). If false, a relative coordinate is expected, and
     *            the method for the same effect must return (1,2).
     */
    protected AbstractMoveNode(final Environment<T, P> environment, final Node<T> node, final boolean isAbsolute) {
        super(node);
        this.environment = environment;
        this.absolute = isAbsolute;
        declareDependencyTo(Dependency.MOVEMENT);
    }

    /**
     * Detects if the move is in absolute or relative coordinates, then calls the correct method on the
     * {@link Environment}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void execute() {
        if (absolute) {
            environment.moveNodeToPosition(getNode(), getNextPosition());
        } else {
            if (environment instanceof EuclideanEnvironment) {
                ((EuclideanEnvironment) environment).moveNode(getNode(), getNextPosition());
            } else {
                environment.moveNodeToPosition(
                        getNode(),
                        environment.getPosition(getNode()).plus(getNextPosition().getCoordinates())
                );
            }
        }
    }

    @Override
    public final Context getContext() {
        return Context.LOCAL;
    }

    /**
     * @return the current environment
     */
    protected Environment<T, P> getEnvironment() {
        return environment;
    }

    /**
     * @return the position of the local node
     */
    protected final P getCurrentPosition() {
        return getNodePosition(getNode());
    }

    /**
     * @return The next position where to move, in absolute or relative coordinates depending on the
     *         value of isAbsolute.
     */
    public abstract P getNextPosition();

    /**
     * Given a node, computes its position.
     * 
     * @param n the node
     * @return the position of the node
     */
    protected final P getNodePosition(final Node<T> n) {
        return environment.getPosition(n);
    }

    /**
     * @return true if this {@link Action} is using absolute positions
     */
    protected final boolean isAbsolute() {
        return absolute;
    }
}
