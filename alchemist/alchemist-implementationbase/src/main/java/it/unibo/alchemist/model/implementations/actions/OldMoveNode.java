/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * This action moves a node inside a given environment.
 * 
 * @param <T>
 */
@Deprecated
public class OldMoveNode<T> extends AbstractMoveNode<T> {

    private static final long serialVersionUID = -5867654295577425307L;

    private Position dir;
    private final Molecule move;

    /**
     * Builds a new move node action.
     * 
     * @param environment
     *            The environment where to move
     * @param node
     *            The node to which this action belongs
     * @param direction
     *            The direction where to move
     * @param movetag
     *            A signal molecule which is useful to maintain dependencies
     *            among reactions which operate physically on the environment
     *            and may be influenced by the move, for instance those
     *            conditions that check the number of neighborhoods. If no
     *            conditions of this kind are present, just pass null.
     */
    public OldMoveNode(final Environment<T> environment, final Node<T> node, final Position direction, final Molecule movetag) {
        super(environment, node);
        dir = direction;
        this.move = movetag;
        addModifiedMolecule(movetag);
    }

    @Override
    public Action<T> cloneOnNewNode(final Node<T> node, final Reaction<T> reaction) {
        return new OldMoveNode<T>(getEnvironment(), node, dir, move);
    }

    @Override
    public void execute() {
        if (dir != null) {
            super.execute();
        }
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    /**
     * @return the set direction
     */
    public Position getDirection() {
        return dir;
    }

    /**
     * @return the signal molecule
     */
    public Molecule getMove() {
        return move;
    }

    /**
     * This method allows to set the direction where to move the node. This must
     * be in relative coordinates with respect to the current node position
     * 
     * @param direction
     *            the direction where to move
     */
    protected void setDirection(final Position direction) {
        this.dir = direction;
    }

    @Override
    public Position getNextPosition() {
        return dir;
    }

}
