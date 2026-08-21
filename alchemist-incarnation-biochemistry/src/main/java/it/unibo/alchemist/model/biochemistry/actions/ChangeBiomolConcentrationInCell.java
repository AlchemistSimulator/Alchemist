/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;
import it.unibo.alchemist.model.actions.AbstractActionOnSingleMolecule;
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule;

import javax.annotation.Nonnull;

/**
 *
 */
public final class ChangeBiomolConcentrationInCell extends AbstractActionOnSingleMolecule<Double> {

    private final double deltaC;

    /**
     * @param biomolecule the molecule
     * @param deltaConcentration the change in concentration
     * @param node the node
     */
    public ChangeBiomolConcentrationInCell(
            final Node<Double> node,
            final Biomolecule biomolecule,
            final double deltaConcentration
    ) {
        super(node, biomolecule);
        if (deltaConcentration == 0) {
            throw new IllegalArgumentException(
                "Changing the concentration of '" + biomolecule + "' of 0 in node " + node.getId() + "makes no sense"
            );
        }
        this.deltaC = deltaConcentration;
    }

    @Nonnull
    @Override
    public ChangeBiomolConcentrationInCell cloneAction(
        @Nonnull final Node<Double> node,
        @Nonnull final NodeReaction<Double> reaction
    ) {
        throw new UnsupportedOperationException("cloneOnNewNode(Node, Reaction) has to be implemented in: " + getClass());
    }

    @Override
    public void execute() {
        super.getNode().setConcentration(getMolecule(), super.getNode().getConcentration(getMolecule()) + deltaC);
    }

    @Nonnull
    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public String toString() {
         if (deltaC >= 0) {
             return getMolecule().getName() + "+" + deltaC;
         } else {
             return getMolecule().getName() + deltaC;
         }
    }

}
