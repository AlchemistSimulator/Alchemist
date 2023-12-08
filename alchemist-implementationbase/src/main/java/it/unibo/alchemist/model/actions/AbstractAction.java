/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions;

import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Dependency;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import org.danilopianini.util.LinkedListSet;
import org.danilopianini.util.ListSet;
import org.danilopianini.util.ListSets;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;


/**
 * An abstract class facility with some generic methods implemented.
 * 
 * @param <T> concentration type
 */
public abstract class AbstractAction<T> implements Action<T> {

    private static final long serialVersionUID = 1L;
    private final ListSet<Dependency> dependencies = new LinkedListSet<>();
    @Nonnull
    private final Node<T> node;

    /**
     * Call this constructor in the subclasses in order to automatically
     * instance the node.
     * 
     * @param node
     *            the node this action belongs to
     */
    protected AbstractAction(@Nonnull final Node<T> node) {
        Objects.requireNonNull(node);
        this.node = node;
    }

    /**
     * Allows to add an Molecule to the set of molecules which are modified by
     * this action. This method must be called in the constructor, and not
     * during the execution.
     * 
     * @param m
     *            the molecule which will be modified
     */
    protected final void declareDependencyTo(final Dependency m) {
        dependencies.add(m);
    }

    /**
     * @param molecule
     *            the molecule
     * @return An {@link Optional} with the value of concentration, or an empty
     *         {@link Optional} if the molecule if
     *         {@link Node#getConcentration(Molecule)} returns null
     */
    protected final Optional<T> getConcentration(final Molecule molecule) {
        return Optional.ofNullable(getNode().getConcentration(molecule));
    }

    /**
     * {@inheritDoc}
     * 
     * How to override: if you intend your action to influence any reaction with
     * compatible context, return null.
     */
    @Nonnull
    @Override
    public final ListSet<? extends Dependency> getOutboundDependencies() {
        return ListSets.unmodifiableListSet(dependencies);
    }

    /**
     * @return the node this action belongs to
     */
    @Nonnull
    protected Node<T> getNode() {
        return node;
    }

    /**
     * Checks if the molecule is contained in this node.
     * @param molecule the molecule
     *
     * @return true if the local node contains the molecule.
     */
    protected final boolean nodeContains(final Molecule molecule) {
        return getNode().contains(molecule);
    }

    /**
     * Deletes a molecule entirely in the local node.
     * 
     * @param molecule molecule
     */
    protected final void removeConcentration(final Molecule molecule) {
        getNode().removeConcentration(Objects.requireNonNull(molecule, "The molecule can not be null"));
    }

    /**
     * Sets the concentration locally.
     * 
     * @param molecule
     *            molecule
     * @param concentration
     *            concentration
     */
    protected final void setConcentration(final Molecule molecule, final T concentration) {
        getNode().setConcentration(
                Objects.requireNonNull(molecule, "The molecule can not be null"),
                Objects.requireNonNull(concentration, "Cannot inject null concentrations"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
