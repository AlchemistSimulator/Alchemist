/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import java.util.List;
import java.util.Map;

import it.unibo.alchemist.model.interfaces.Dependency;
import org.danilopianini.lang.HashString;

import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaAction;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;


/**
 * 
 */
public final class LsaDeleteNodeAction extends RemoveNode<List<ILsaMolecule>> implements ILsaAction {

    private static final long serialVersionUID = -7128058274012426458L;

    /**
     * @param environment the current environment
     * @param node the node for this action
     */
    public LsaDeleteNodeAction(final Environment<List<ILsaMolecule>, ?> environment, final ILsaNode node) {
        super(environment, node);
        declareDependencyTo(Dependency.MOVEMENT);
    }

    /* (non-Javadoc)
     * @see alice.alchemist.model.implementations.actions.LsaAbstractAction#cloneOnNewNode(alice.alchemist.model.interfaces.Node, alice.alchemist.model.interfaces.Reaction)
     */
    @Override
    public LsaDeleteNodeAction cloneAction(final Node<List<ILsaMolecule>> n, final Reaction<List<ILsaMolecule>> r) {
        return new LsaDeleteNodeAction(getEnvironment(), (ILsaNode) n);
    }

    @Override
    public void setExecutionContext(final Map<HashString, ITreeNode<?>> matches, final List<ILsaNode> nodes) {
        /*
         * This reaction runs regardless the context.
         */
    }

}
