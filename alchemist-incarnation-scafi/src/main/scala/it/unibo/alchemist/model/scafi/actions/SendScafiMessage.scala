/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.scafi.actions

import it.unibo.alchemist.model._
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.incarnations.ScafiIncarnationUtils
import it.unibo.alchemist.model.incarnations.ScafiIncarnationUtils.runInScafiDeviceContext

import java.util.stream.Collectors
import scala.jdk.CollectionConverters._

class SendScafiMessage[T, P <: Position[P]](
    environment: Environment[T, P],
    reaction: NodeReaction[T],
    val program: RunScafiProgram[T, P]
) extends AbstractLocalAction[T](reaction) {
  assert(program != null, "Program cannot be null")

  /**
   * This method allows to clone this action on a new reaction, sending the messages of the only [[RunScafiProgram]] of
   * its node. It may result useful to support runtime creation of nodes with the same reaction programming, e.g. for
   * morphogenesis.
   *
   * @param newReaction
   *   The reaction that will own the cloned action
   * @return
   *   the cloned action
   */
  override protected def cloneOnNodeReaction(newReaction: NodeReaction[T]): Action[T] = {
    val destinationNode = newReaction.getHost
    runInScafiDeviceContext[T, Action[T]](
      node = destinationNode,
      message =
        getClass.getSimpleName + " cannot get cloned on a node of type " + destinationNode.getClass.getSimpleName,
      _ => {
        val possibleRef = destinationNode.getReactions.getCurrent
          .stream()
          .flatMap(reaction => reaction.getActions.stream())
          .filter(action => action.isInstanceOf[RunScafiProgram[_, _]])
          .map(action => action.asInstanceOf[RunScafiProgram[T, P]])
          .collect(Collectors.toList[RunScafiProgram[T, P]])
        if (possibleRef.size() == 1) {
          return new SendScafiMessage(environment, newReaction, possibleRef.get(0))
        }
        throw new IllegalStateException(
          "There must be one and one only unconfigured " + RunScafiProgram.getClass.getSimpleName
        )
      }
    )
  }

  /** Effectively executes this action. */
  override def execute(): Unit = {
    val toSend = program.getExport(getTargetNode.getId).get
    for {
      neighborhood <- environment.getNeighborhood(getTargetNode).getCurrent.getNeighbors.iterator().asScala
      action <- ScafiIncarnationUtils.allScafiProgramsFor[T, P](neighborhood).filter(program.getClass.isInstance(_))
      if action.programNameMolecule == program.programNameMolecule
    } action.sendExport(getTargetNode.getId, toSend)
    program.prepareForComputationalCycle
  }
}
