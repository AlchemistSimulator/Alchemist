/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.{Node, Position, Reaction, ScafiIncarnationUtils}
import it.unibo.alchemist.model.ScafiIncarnationUtils._
import it.unibo.alchemist.model.implementations.nodes.ScafiDevice
import it.unibo.alchemist.model._
import it.unibo.alchemist.model.actions.AbstractAction

import java.util.stream.Collectors
import scala.jdk.CollectionConverters._

class SendScafiMessage[T, P <: Position[P]](
    environment: Environment[T, P],
    device: ScafiDevice[T],
    reaction: Reaction[T],
    val program: RunScafiProgram[T, P]
) extends AbstractAction[T](device.getNode) {
  assert(reaction != null, "Reaction cannot be null")
  assert(program != null, "Program cannot be null")

  /**
   * This method allows to clone this action on a new node. It may result useful to support runtime creation of nodes
   * with the same reaction programming, e.g. for morphogenesis.
   *
   * @param destinationNode
   *   The node where to clone this { @link Action}
   * @param reaction
   *   The reaction to which the CURRENT action is assigned
   * @return
   *   the cloned action
   */
  override def cloneAction(destinationNode: Node[T], reaction: Reaction[T]): Action[T] =
    runInScafiDeviceContext[T, Action[T]](
      node = destinationNode,
      message =
        getClass.getSimpleName + " cannot get cloned on a node of type " + destinationNode.getClass.getSimpleName,
      device => {
        val possibleRef = destinationNode.getReactions
          .stream()
          .flatMap(reaction => reaction.getActions.stream())
          .filter(action => action.isInstanceOf[RunScafiProgram[_, _]])
          .map(action => action.asInstanceOf[RunScafiProgram[T, P]])
          .collect(Collectors.toList[RunScafiProgram[T, P]])
        if (possibleRef.size() == 1) {
          return new SendScafiMessage(environment, device, reaction, possibleRef.get(0))
        }
        throw new IllegalStateException(
          "There must be one and one only unconfigured " + RunScafiProgram.getClass.getSimpleName
        )
      }
    )

  /** Effectively executes this action. */
  override def execute(): Unit = {
    val toSend = program.getExport(device.getNode.getId).get
    for {
      neighborhood <- environment.getNeighborhood(device.getNode).getNeighbors.iterator().asScala
      action <- ScafiIncarnationUtils.allScafiProgramsFor[T, P](neighborhood).filter(program.getClass.isInstance(_))
      if action.programNameMolecule == program.programNameMolecule
    } action.sendExport(device.getNode.getId, toSend)
    program.prepareForComputationalCycle
  }

  /** @return The context for this action. */
  override def getContext: Context = Context.NEIGHBORHOOD
}
