/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.ScafiIncarnationUtils
import it.unibo.alchemist.model.implementations.nodes.ScafiNode
import it.unibo.alchemist.model.interfaces._

import java.util.ArrayList

class SendScafiMessage[T, P<:Position[P]](
  env: Environment[T, P],
  node: ScafiNode[T, P],
  reaction: Reaction[T],
  val program: RunScafiProgram[T, P]
) extends AbstractAction[T](node) {
  assert(reaction != null, "Reaction cannot be null")
  assert(program != null, "Program cannot be null")

  /**
   * This method allows to clone this action on a new node. It may result
   * useful to support runtime creation of nodes with the same reaction
   * programming, e.g. for morphogenesis.
   *
   * @param n
   * The node where to clone this { @link Action}
   * @param r
   * The reaction to which the CURRENT action is assigned
   * @return the cloned action
   */
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = {
    n match {
      case destinationNode: ScafiNode[T, P] =>
        val possibleRef = new ArrayList[RunScafiProgram[T, P]]()
        destinationNode.getReactions.stream()
          .flatMap { reaction => reaction.getActions.stream() }
          .filter { action => action.isInstanceOf[RunScafiProgram[T, P]] }
          .map { action => action.asInstanceOf[RunScafiProgram[T, P]] }
          .forEach(act => possibleRef.add(act))
        if (possibleRef.size() == 1) {
          return new SendScafiMessage(env, destinationNode, reaction, possibleRef.get(0))
        }
        throw new IllegalStateException("There must be one and one only unconfigured " + RunScafiProgram.getClass.getSimpleName)
      case _ => throw new IllegalStateException(getClass.getSimpleName + " cannot get cloned on a node of type " + node.getClass.getSimpleName)
    }
  }

  /**
   * Effectively executes this action.
   */
  override def execute(): Unit = {
    import scala.jdk.CollectionConverters._
    val toSend = program.getExport(node.getId).get
    for (
      nbr <- env.getNeighborhood(node).getNeighbors.iterator().asScala;
      action <- ScafiIncarnationUtils.allScafiProgramsFor[T, P](nbr).filter(program.getClass.isInstance(_))) {
      action.asInstanceOf[RunScafiProgram[T, P]].sendExport(node.getId, toSend)
    }
    program.prepareForComputationalCycle
  }

  /**
   * @return The context for this action.
   */
  override def getContext: Context = Context.NEIGHBORHOOD
}
