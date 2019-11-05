/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.implementations.conditions

import it.unibo.alchemist.implementations.nodes.ScafiNode
import it.unibo.alchemist.implementations.actions.RunScafiProgram
import it.unibo.alchemist.model.ScafiIncarnationUtils
import it.unibo.alchemist.model.implementations.conditions.AbstractCondition
import it.unibo.alchemist.model.interfaces.{Condition, Context, Node, Reaction}

final class ScafiComputationalRoundComplete[T](val node: Node[T], val program: RunScafiProgram[_,_])
  extends AbstractCondition(node) {
  declareDependencyOn(this.program.asMolecule)

  override def cloneCondition(n: Node[T], r: Reaction[T]): Condition[T] = {
    if (!n.isInstanceOf[ScafiNode[_,_]]) {
      throw new IllegalStateException(getClass.getSimpleName + " cannot get cloned on a node of type " + n.getClass.getSimpleName)
    }

    val scafiNode = n.asInstanceOf[ScafiNode[_,_]]
    val possibleRefs: Iterable[RunScafiProgram[_,_]] = ScafiIncarnationUtils.possibleRefs(n)
      if (possibleRefs.size == 1)
        new ScafiComputationalRoundComplete(n, possibleRefs.head)
      else
        throw new IllegalStateException("There must be one and one only unconfigured " + classOf[Nothing].getSimpleName)
  }

  override def getContext = Context.LOCAL

  override def getPropensityContribution = if (isValid) 1 else 0

  override def isValid = program.isComputationalCycleComplete

  override def getNode = super.getNode.asInstanceOf[ScafiNode[T,_]]

  override def toString = program.asMolecule.getName + " completed round"
}