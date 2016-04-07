/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaAction;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.danilopianini.lang.util.FasterString;


/**
 * 
 */
public class LsaDeleteNodeAction extends RemoveNode<List<? extends ILsaMolecule>> implements ILsaAction {

    private static final long serialVersionUID = -7128058274012426458L;

    /**
     * @param environment the current environment
     * @param node the node for this action
     */
    public LsaDeleteNodeAction(final Environment<List<? extends ILsaMolecule>> environment, final ILsaNode node) {
        super(environment, node);
    }

    /* (non-Javadoc)
     * @see alice.alchemist.model.implementations.actions.LsaAbstractAction#cloneOnNewNode(alice.alchemist.model.interfaces.Node, alice.alchemist.model.interfaces.Reaction)
     */
    @Override
    public LsaDeleteNodeAction cloneOnNewNode(final Node<List<? extends ILsaMolecule>> n, final Reaction<List<? extends ILsaMolecule>> r) {
        return new LsaDeleteNodeAction(getEnvironment(), (ILsaNode) n);
    }

    @Override
    public List<ILsaMolecule> getModifiedMolecules() {
        return Collections.emptyList();
    }

    @Override
    public void setExecutionContext(final Map<FasterString, ITreeNode<?>> matches, final List<? extends ILsaNode> nodes) {
        /*
         * This reaction runs regardless the context.
         */
    }

}
