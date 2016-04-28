/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

/**
 *          Interface for a molecule.
 * 
 */
public interface Molecule extends Serializable {

    /**
     * Allows to access the id of a molecule.
     * 
     * @return the id of the molecule
     */
    long getId();

    /**
     * Calculates the dependency of an {@link Molecule} against another.
     * 
     * @param mol the molecole to verify the dependency
     * @return true if this molecule generates a dependency with mol
     */
    boolean dependsOn(Molecule mol);

}
