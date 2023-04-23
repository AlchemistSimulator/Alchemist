/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.molecules;

import it.unibo.alchemist.model.molecules.SimpleMolecule;

/**
 *
 */

public class Biomolecule extends SimpleMolecule {

    private static final long serialVersionUID = 8666013848795443487L;

    /**
     * Create a new biomolecule.
     * @param name  the molecule name
     */
    public Biomolecule(final CharSequence name) {
        super(name);
    }

}
