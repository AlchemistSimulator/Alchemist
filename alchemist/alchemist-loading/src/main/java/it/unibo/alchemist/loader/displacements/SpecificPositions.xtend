/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.loader.displacements;

import java.util.stream.Stream;

import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Environment
import java.util.List

class SpecificPositions<P extends Position<? extends P>> implements Displacement<P> {
	
	List<P> positions;

    new(Environment<?, P> env, Iterable<? extends Number>... positions) {
    	this.positions = positions.map[env.makePosition(it)].toList
    }

    override Stream<P> stream() {
        return positions.stream;
    }

}
