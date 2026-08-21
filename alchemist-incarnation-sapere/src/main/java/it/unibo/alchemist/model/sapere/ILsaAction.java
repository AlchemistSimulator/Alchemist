/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere;

import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;
import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import org.danilopianini.lang.HashString;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 */
public interface ILsaAction extends Action<List<ILsaMolecule>> {

    /**
     * Sets the context in which this action will execute.
     *
     * @param matches
     *            the computed matches
     * @param nodes
     *            the nodes allowed for this action
     */
    void setExecutionContext(Map<HashString, ITreeNode<?>> matches, List<ILsaNode> nodes);

    @Nonnull
    @Override
    ILsaAction cloneAction(
        @Nonnull Node<List<ILsaMolecule>> node,
        @Nonnull NodeReaction<List<ILsaMolecule>> reaction
    );

}
