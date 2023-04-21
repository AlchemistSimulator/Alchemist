/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.concentrations;

import it.unibo.alchemist.model.Concentration;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;

import java.util.Collections;
import java.util.List;


/**
 * This class acts as a fake concentration. It is required in order to enforce
 * compatibility with the basic Alchemist model
 * 
 */
public final class LsaConcentration implements Concentration<List<? extends ILsaMolecule>> {

    private static final long serialVersionUID = -5225528630199110508L;

    @Override
    public List<? extends ILsaMolecule> getContent() {
        return Collections.emptyList();
    }

}
