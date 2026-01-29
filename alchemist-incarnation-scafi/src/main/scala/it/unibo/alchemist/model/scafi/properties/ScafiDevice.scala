/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.scafi.properties

import it.unibo.alchemist.model.scafi.actions.{RunScafiProgram, SendScafiMessage}
import it.unibo.alchemist.model.{Node, NodeProperty}
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

class ScafiDevice[E](node: Node[E]) extends NodeProperty[E] {
  override def getNode: Node[E] = node

  override def cloneOnNewNode(node: Node[E]): NodeProperty[E] = new ScafiDevice[E](node)

  /**
   * Validates that the node has the required send actions for communication. Warns the user if ScafiDevice nodes are
   * missing SendScafiMessage actions.
   */
  def validateCommunicationConfiguration(): Unit = {
    val hasScafiPrograms = node.getReactions.asScala
      .flatMap(_.getActions.asScala)
      .exists(_.isInstanceOf[RunScafiProgram[_, _]])

    if (hasScafiPrograms) {
      val hasSendAction = node.getReactions.asScala
        .flatMap(_.getActions.asScala)
        .exists(_.isInstanceOf[SendScafiMessage[_, _]])

      if (!hasSendAction) {
        ScafiDevice.LOGGER.warn(
          "Scafi node {} is missing a 'send' action. This node will not be able to " +
            "communicate with neighboring nodes. Consider adding a reaction with 'send' action " +
            "to enable communication.",
          node.getId
        )
      }
    }
  }
}

object ScafiDevice {
  private val LOGGER = LoggerFactory.getLogger(classOf[ScafiDevice[_]])
}
