/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.scafi.conditions

import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.incarnations.ScafiIncarnationUtils
import it.unibo.alchemist.model.scafi.actions.RunScafiProgram
import it.unibo.alchemist.model.scafi.properties.ScafiDevice
import it.unibo.alchemist.model.{Condition, Context, Node, Reaction}

final class ScafiComputationalRoundComplete[T](val device: ScafiDevice[T], val program: RunScafiProgram[?, ?])
    extends AbstractCondition(device.getNode):
  declareDependencyOn(this.program.asMolecule)

  override def cloneCondition(node: Node[T], reaction: Reaction[T]): Condition[T] =
    ScafiIncarnationUtils.runInScafiDeviceContext[T, Condition[T]](
      node,
      getClass.getSimpleName + " cannot get cloned on a node of type " + node.getClass.getSimpleName,
      device =>
        val possibleRefs: Iterable[RunScafiProgram[?, ?]] = ScafiIncarnationUtils.allScafiProgramsFor(device.getNode)
        if possibleRefs.size == 1 then new ScafiComputationalRoundComplete(device, possibleRefs.head)
        else
          throw new IllegalStateException(
            "There must be one and one only unconfigured " + classOf[Nothing].getSimpleName
          )
    )

  override def getContext = Context.LOCAL

  override def getPropensityContribution: Double = if isValid then 1 else 0

  override def isValid = program.isComputationalCycleComplete

  override def getNode = super.getNode

  override def toString = program.asMolecule.getName + " completed round"
