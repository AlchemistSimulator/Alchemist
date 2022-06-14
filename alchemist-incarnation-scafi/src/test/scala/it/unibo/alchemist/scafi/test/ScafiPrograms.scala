/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.scafi.test

import java.time.ZoneOffset

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scafi.space.Point3D

import scala.concurrent.duration.FiniteDuration

class ScafiGradientProgram extends AggregateProgram with StandardSensorNames {
  override def main(): Double = gradient(sense[Boolean]("source"))

  def gradient(source: Boolean): Double =
    rep(Double.PositiveInfinity) { distance =>
      mux(source)(0.0) {
        foldhood(Double.PositiveInfinity)(Math.min)(nbr(distance) + nbrvar[Double](NBR_RANGE))
      }
    }
}

class ScafiEnvProgram extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with FieldUtils {
  import ScafiEnvProgram._

  override def main(): Any = {
    node.put("number2", node.get[Int]("number") + 100)

    val itimestamp: java.time.Instant = currentTime()
    val deltaManual = java.time.Instant.ofEpochMilli(
      rep((itimestamp, 0L))(old => (itimestamp, itimestamp.toEpochMilli - old._1.toEpochMilli))._2
    )
    val delta: FiniteDuration = deltaTime()
    val nbrLagField = excludingSelf.reifyField(nbrLag()).values.headOption
    val nbrRangeField = excludingSelf.reifyField(nbrRange()).values.headOption
    val nbrVectorField = excludingSelf.reifyField(nbrVector()).values.headOption
    val pos: Point3D = currentPosition()

    node.put(MOL_POSITION, pos)
    node.put(MOL_TIMESTAMP, itimestamp)
    node.put(MOL_DELTA_MANUAL_MILLIS, deltaManual.toEpochMilli)
    node.put(MOL_DELTATIME, delta)
    node.put(MOL_NBR_LAG, nbrLagField)
    node.put(MOL_NBR_RANGE, nbrRangeField)
    node.put(MOL_NBR_VECTOR, nbrVectorField)

    node.put(
      "out",
      // Local sensors
      s"CONTENTS: ${mid()}: \nrandomgen nextdouble " + randomGenerator().nextDouble() +
        "\nalchemistenv getdimensions " + alchemistEnvironment.getDimensions +
        "\ndeltatime " + deltaTime() +
        "\ncurrentTime toEpochMilli " + currentTime().toEpochMilli +
        "\ntimestamp() " + timestamp() +
        "\nnextRandom " + nextRandom() +
        // Alchemist-specific local sensors
        "\nnot next boolean " + (!alchemistRandomGen.nextBoolean()) +
        "\nalchemistCoordinates toSeq " + alchemistCoordinates.toSeq +
        "\nalchemistDeltaTime " + alchemistDeltaTime() +
        "\nalchemistTimestamp toDouble " + alchemistTimestamp.toDouble +
        // Environmental sensors
        "\nnbrLag " + includingSelf.reifyField(nbrLag()) +
        "\nnbrRange " + includingSelf.reifyField(nbrRange()) +
        "\nnbrDelay" + includingSelf.reifyField(nbrDelay()) +
        "\nnbrVector" + includingSelf.reifyField(nbrVector())
    )
  }
}
object ScafiEnvProgram {
  val MOL_TIMESTAMP = "timestamp"
  val MOL_DELTA_MANUAL_MILLIS = "deltaManualEpochMilli"
  val MOL_DELTATIME = "deltatime"
  val MOL_NBR_VECTOR = "nbrvec"
  val MOL_NBR_RANGE = "nbrran"
  val MOL_NBR_LAG = "nbrlag"
  val MOL_POSITION = "pos"
}

object MyMain extends App {
  val program = new ScafiGradientProgram()
  program.round(new ContextImpl(1, Map(), Map("source" -> true), Map("nbrRange" -> Map(1 -> 0))))
}
