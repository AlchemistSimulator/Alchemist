/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import it.unibo.alchemist.expressions.interfaces.ITreeNode;

import java.util.List;
import java.util.Map;

import org.danilopianini.lang.util.FasterString;

/**
 */
public interface ILsaAction extends Action<List<? extends ILsaMolecule>> {

    /**
     * Sets the context in which this action will execute.
     * 
     * @param matches
     *            the computed matches
     * @param nodes
     *            the nodes allowed for this action
     */
    void setExecutionContext(final Map<FasterString, ITreeNode<?>> matches, List<? extends ILsaNode> nodes);

    @Override
    List<? extends ILsaMolecule> getModifiedMolecules();

    @Override
    ILsaAction cloneOnNewNode(Node<List<? extends ILsaMolecule>> n, Reaction<List<? extends ILsaMolecule>> r);

}
