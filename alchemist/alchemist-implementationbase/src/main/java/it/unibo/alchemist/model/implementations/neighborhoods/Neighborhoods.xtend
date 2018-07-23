/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import java.util.Collections
import it.unibo.alchemist.model.interfaces.Position

final class Neighborhoods {

	private new() {}

	def static <T, P extends Position<P>> Neighborhood<T> make(Environment<T, P> env, Node<T> center) {
		make(env, center, Collections.emptyList)
	}

	def static <T, P extends Position<P>> Neighborhood<T> make(Environment<T, P> env, Node<T> center, Iterable<? extends Node<T>> neighbors) {
		new SimpleNeighborhood(env, center, neighbors)
	}

}
