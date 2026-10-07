/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.scafi.test

import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.{Action, NodeReaction}

class FooAction(reaction: NodeReaction[Any], moleculeName: String) extends AbstractLocalAction[Any](reaction) {
  override protected def cloneOnNodeReaction(newReaction: NodeReaction[Any]): Action[Any] =
    new FooAction(newReaction, moleculeName)
  override def execute(): Unit = getTargetNode.getConcentration(new SimpleMolecule(moleculeName))
}
