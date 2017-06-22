package it.unibo.alchemist.implementation.nodes

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Node}

trait NodeManager {
  def put[T](molecule: String, concentration: T)
  def get[T](molecule: String): T
}
class SimpleNodeManager(val node: Node[Any]) extends NodeManager {
  override def put[T](molecule: String, concentration: T): Unit = node.setConcentration(new SimpleMolecule(molecule), concentration)

  override def get[T](molecule: String): T = node.getConcentration(new SimpleMolecule(molecule)).asInstanceOf[T]
}
