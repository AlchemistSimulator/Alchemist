/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.scafi.conditions

import it.unibo.alchemist.model.conditions.AbstractLocalCondition
import it.unibo.alchemist.model.incarnations.ScafiIncarnationUtils
import it.unibo.alchemist.model.scafi.actions.RunScafiProgram
import it.unibo.alchemist.model.{Condition, NodeReaction}

final class ScafiComputationalRoundComplete[T](reaction: NodeReaction[T], val program: RunScafiProgram[?, ?])
    extends AbstractLocalCondition[T](reaction):
  setValidity(program.observeComputationalCycleComplete.map(valid => java.lang.Boolean.valueOf(valid)))

  override protected def cloneOnNodeReaction(newReaction: NodeReaction[T]): Condition[T] =
    val node = newReaction.getHost
    ScafiIncarnationUtils.runInScafiDeviceContext[T, Condition[T]](
      node,
      getClass.getSimpleName + " cannot get cloned on a node of type " + node.getClass.getSimpleName,
      device =>
        val possibleRefs: Iterable[RunScafiProgram[?, ?]] = ScafiIncarnationUtils.allScafiProgramsFor(device.getNode)
        if possibleRefs.size == 1 then new ScafiComputationalRoundComplete(newReaction, possibleRefs.head)
        else
          throw new IllegalStateException(
            "There must be one and one only unconfigured " + classOf[Nothing].getSimpleName
          )
    )

  override def toString: String = program.asMolecule.getName + " completed round"
