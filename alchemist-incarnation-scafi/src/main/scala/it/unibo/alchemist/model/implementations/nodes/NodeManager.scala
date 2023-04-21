/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule

trait NodeManager {
  def put[T](molecule: String, concentration: T): Unit
  def get[T](molecule: String): T
  def getOption[V](molecule: String): Option[V]
  def has(molecule: String): Boolean
  def remove(molecule: String): Unit
  def getOrElse[T](molecule: String, defaultValue: => T): T = getOption(molecule).getOrElse(defaultValue)
}

class SimpleNodeManager[T](val node: Node[T]) extends NodeManager {
  override def put[V](molecule: String, concentration: V): Unit =
    node.setConcentration(new SimpleMolecule(molecule), concentration.asInstanceOf[T])

  override def get[V](molecule: String): V = node.getConcentration(new SimpleMolecule(molecule)).asInstanceOf[V]

  override def getOption[V](molecule: String): Option[V] = if (has(molecule)) Some[V](get(molecule)) else None

  override def has(molecule: String): Boolean = node.contains(new SimpleMolecule(molecule))

  override def remove(molecule: String): Unit = node.removeConcentration(new SimpleMolecule(molecule))
}
