package it.unibo.alchemist.implementation.nodes

import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.Environment

class ScafiNode(env: Environment[_, _]) extends AbstractNode[Any](env) {

  override def createT = new {}

}