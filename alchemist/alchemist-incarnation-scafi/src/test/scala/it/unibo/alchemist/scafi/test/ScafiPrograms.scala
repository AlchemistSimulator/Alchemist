/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.scafi.test

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class ScafiGradientProgram extends AggregateProgram {
  override def main(): Double = gradient(sense[Boolean]("source"))

  def gradient(source: Boolean): Double =
    rep(Double.PositiveInfinity){
      distance => mux(source) { 0.0 } {
        foldhood(Double.PositiveInfinity)(Math.min)(nbr{distance}+nbrvar[Double](NBR_RANGE))
      }
    }
}

class ScafiEnvProgram extends AggregateProgram with StandardSensors with ScafiAlchemistSupport {
  override def main(): Any = {
    node.put("number2", node.get[Int]("number")+100)

    node.put("out", "" +
    // Local sensors
    randomGenerator().nextDouble() +
    alchemistEnvironment.getDimensions +
    deltaTime().length +
    currentTime().getDayOfMonth +
    timestamp() +
    (nextRandom+0.0) +

    // Alchemist-specific local sensors
    (!alchemistRandomGen.nextBoolean()) +
    alchemistCoordinates.head +
    alchemistDeltaTime(whenNan = 0.0).longValue() +
    alchemistTimestamp.toDouble +

    // Environmental sensors
    foldhood("")(_+_){
      "" +
      nbrLag().length +
      nbrRange().toString +
      nbrDelay().length +
      nbrVector().y
    })
  }
}

object MyMain extends App {
  val program = new ScafiGradientProgram()
  program.round(new ContextImpl(1, Map(), Map("source"->true), Map("nbrRange" -> Map(1 -> 0))))
}
