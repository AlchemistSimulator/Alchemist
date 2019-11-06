/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Node}

trait NodeManager {
  def put[T](molecule: String, concentration: T)
  def get[T](molecule: String): T
  def has(molecule: String)
}

class SimpleNodeManager[T](val node: Node[T]) extends NodeManager {
  override def put[V](molecule: String, concentration: V): Unit = node.setConcentration(new SimpleMolecule(molecule), concentration.asInstanceOf[T])

  override def get[V](molecule: String): V = node.getConcentration(new SimpleMolecule(molecule)).asInstanceOf[V]

  override def has(molecule: String): Unit = node.contains(new SimpleMolecule(molecule))
}
