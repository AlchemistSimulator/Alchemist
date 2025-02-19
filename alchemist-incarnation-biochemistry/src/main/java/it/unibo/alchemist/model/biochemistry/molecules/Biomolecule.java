/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.molecules;

import it.unibo.alchemist.model.molecules.SimpleMolecule;

import java.io.Serial;

/**
 *
 */
public class Biomolecule extends SimpleMolecule {

    @Serial
    private static final long serialVersionUID = 8666013848795443487L;

    /**
     * Create a new biomolecule.
     *
     * @param name  the molecule name
     */
    public Biomolecule(final CharSequence name) {
        super(name);
    }

}
