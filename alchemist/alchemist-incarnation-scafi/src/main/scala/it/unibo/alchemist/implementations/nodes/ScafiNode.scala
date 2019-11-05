/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.implementations.nodes

import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.{Environment, Position}

class ScafiNode[T,P<:Position[P]](env: Environment[T, P]) extends AbstractNode[T](env) {

  override def createT = throw new Exception("The molecule does not exist and cannot create empty concentration")

}
