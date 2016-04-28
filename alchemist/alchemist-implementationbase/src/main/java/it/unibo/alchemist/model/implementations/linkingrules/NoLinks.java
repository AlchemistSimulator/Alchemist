/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.implementations.neighborhoods.CachedNeighborhood;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;

import java.util.Collections;

/**
 * This rule guarantees that no links are created at all.
 * 
 * @param <T>
 */
public class NoLinks<T> implements LinkingRule<T> {

    private static final long serialVersionUID = -711043794655618585L;

    @Override
    public Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T> env) {
        return new CachedNeighborhood<>(center, Collections.emptyList(), env);
    }

}
