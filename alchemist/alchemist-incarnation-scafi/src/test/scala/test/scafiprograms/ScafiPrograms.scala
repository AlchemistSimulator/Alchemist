/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package test.scafiprograms

import it.unibo.alchemist.implementation.nodes.NodeManager
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

trait ScafiAlchemistSupport { self: AggregateProgram =>
  def env = sense[NodeManager]("manager")
}

class ScafiGradientProgram extends AggregateProgram {
  override type MainResult = Double
  override def main(): Double = gradient(sense[Boolean]("source"))

  def gradient(source: Boolean): Double =
    rep(Double.PositiveInfinity){
      distance => mux(source) { 0.0 } {
        foldhood(Double.PositiveInfinity)(Math.min)(nbr{distance}+nbrvar[Double](NBR_RANGE_NAME))
      }
    }
}

class ScafiEnvProgram extends AggregateProgram with ScafiAlchemistSupport {
  override type MainResult = Any
  override def main(): Any = {
    env.put("number2", env.get[Int]("number")+100)
  }
}

object MyMain extends App {
  val program = new ScafiGradientProgram()
  program.round(new ContextImpl(1, Map(), Map("source"->true), Map("nbrRange" -> Map(1 -> 0))))
}
