/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.scafi.test

import it.unibo.alchemist.model.{Node, Reaction}
import it.unibo.alchemist.model.implementations.actions.AbstractAction
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.{Action, Context}

class FooAction(val node: Node[Any], moleculeName: String) extends AbstractAction[Any](node) {
  override def cloneAction(node: Node[Any], reaction: Reaction[Any]): Action[Any] = new FooAction(node, moleculeName)
  override def execute(): Unit = node.getConcentration(new SimpleMolecule(moleculeName))
  override def getContext: Context = Context.LOCAL
}
