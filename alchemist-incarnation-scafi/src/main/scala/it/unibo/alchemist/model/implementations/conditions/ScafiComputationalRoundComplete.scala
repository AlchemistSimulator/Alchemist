/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions

import it.unibo.alchemist.model.ScafiIncarnationUtils
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.implementations.actions.RunScafiProgram
import it.unibo.alchemist.model.implementations.nodes.ScafiDevice
import it.unibo.alchemist.model.{Condition, Context, Node, Reaction}

final class ScafiComputationalRoundComplete[T](val device: ScafiDevice[T], val program: RunScafiProgram[_, _])
    extends AbstractCondition(device.getNode) {
  declareDependencyOn(this.program.asMolecule)

  override def cloneCondition(node: Node[T], reaction: Reaction[T]): Condition[T] = {
    ScafiIncarnationUtils.runInScafiDeviceContext[T, Condition[T]](
      node,
      getClass.getSimpleName + " cannot get cloned on a node of type " + node.getClass.getSimpleName,
      device => {
        val possibleRefs: Iterable[RunScafiProgram[_, _]] = ScafiIncarnationUtils.allScafiProgramsFor(device.getNode)
        if (possibleRefs.size == 1) {
          new ScafiComputationalRoundComplete(device, possibleRefs.head)
        } else {
          throw new IllegalStateException(
            "There must be one and one only unconfigured " + classOf[Nothing].getSimpleName
          )
        }
      }
    )
  }

  override def getContext = Context.LOCAL

  override def getPropensityContribution = if (isValid) 1 else 0

  override def isValid = program.isComputationalCycleComplete

  override def getNode = super.getNode

  override def toString = program.asMolecule.getName + " completed round"
}
