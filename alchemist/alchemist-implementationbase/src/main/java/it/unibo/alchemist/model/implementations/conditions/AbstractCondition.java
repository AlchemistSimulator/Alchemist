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
package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import java8.util.Objects;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @param <T>
 */
public abstract class AbstractCondition<T> implements Condition<T> {

    private static final long serialVersionUID = -1610947908159507754L;
    private final List<Molecule> influencing = new ArrayList<Molecule>(1);
    private final Node<T> n;

    /**
     * @param node the node this Condition belongs to
     */
    public AbstractCondition(final Node<T> node) {
        this.n = Objects.requireNonNull(node);
    }

    @Override
    public List<? extends Molecule> getInfluencingMolecules() {
        return influencing;
    }

    @Override
    public Node<T> getNode() {
        return n;
    }

    /**
     * @param m the molecule to add
     */
    protected void addReadMolecule(final Molecule m) {
        influencing.add(m);
    }
}
