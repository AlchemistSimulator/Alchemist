/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.scafi

import it.unibo.alchemist.implementation.nodes.NodeManager
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.{Environment, Position}
import it.unibo.scafi.PlatformDependentConstants
import it.unibo.scafi.incarnations.BasicAbstractIncarnation
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point3D}
import it.unibo.scafi.time.BasicTimeAbstraction
import org.apache.commons.math3.random.RandomGenerator

import scala.util.Random

object ScafiIncarnationForAlchemist extends BasicAbstractIncarnation
  with StandardLibrary with BasicTimeAbstraction with BasicSpatialAbstraction {
  override type P = Point3D
  override implicit val idBounded = Builtins.Bounded.of_i

  val LSNS_ALCHEMIST_NODE_MANAGER = "manager"
  val LSNS_ALCHEMIST_ENVIRONMENT = "environment"
  val LSNS_ALCHEMIST_COORDINATES = "coordinates"
  val LSNS_ALCHEMIST_RANDOM = "alchemistRandomGen"
  val LSNS_ALCHEMIST_DELTA_TIME = "alchemistDeltaTime"
  val LSNS_ALCHEMIST_TIMESTAMP = "alchemistTimestamp"
  val NBR_ALCHEMIST_DELAY = "alchemistNbrDelay"
  val NBR_ALCHEMIST_LAG = "alchemistNbrLag"

  class AlchemistRandomWrapper(val rg: RandomGenerator) extends Random {
    override def nextBoolean(): Boolean = rg.nextBoolean()
    override def nextDouble(): Double = rg.nextDouble()
    override def nextInt(): Int = rg.nextInt()
    override def nextInt(n: Int): Int = rg.nextInt(n)
    override def nextFloat(): Float = rg.nextFloat()
    override def nextLong(): Long = rg.nextLong()
    override def nextGaussian(): Double = rg.nextGaussian()

    override def clone(): AnyRef = new AlchemistRandomWrapper(rg)
  }

  trait ScafiAlchemistSupport { self: AggregateProgram with StandardSensors =>
    def node = sense[NodeManager](LSNS_ALCHEMIST_NODE_MANAGER)

    def alchemistCoordinates = sense[Array[Double]](LSNS_ALCHEMIST_COORDINATES)

    def alchemistDeltaTime(whenNan: Double = Double.NaN): Double = {
      val dt = sense[DoubleTime](LSNS_ALCHEMIST_DELTA_TIME).toDouble
      if(dt.isNaN) whenNan else dt
    }

    def alchemistTimestamp = sense[it.unibo.alchemist.model.interfaces.Time](LSNS_ALCHEMIST_TIMESTAMP)

    //def nextRandom: Double = sense[RandomGenerator](LSNS_RANDOM).nextDouble()
    def alchemistRandomGen = sense[RandomGenerator](LSNS_ALCHEMIST_RANDOM)
    lazy val randomGen: Random = new AlchemistRandomWrapper(alchemistRandomGen)
    override def randomGenerator(): Random = randomGen
    override def nextRandom(): Double = alchemistRandomGen.nextDouble()

    def alchemistEnvironment = sense[Environment[Any,Position[_]]](LSNS_ALCHEMIST_ENVIRONMENT)
  }
}
