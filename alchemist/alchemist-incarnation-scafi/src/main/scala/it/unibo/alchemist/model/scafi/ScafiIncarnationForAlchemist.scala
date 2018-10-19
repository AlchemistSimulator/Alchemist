/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.scafi

import it.unibo.alchemist.implementation.nodes.NodeManager
import it.unibo.alchemist.model.interfaces.{Environment, Position}
import it.unibo.scafi.PlatformDependentConstants
import it.unibo.scafi.incarnations.BasicAbstractIncarnation
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point3D}
import it.unibo.scafi.time.BasicTimeAbstraction
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.util.DoubleArray

import scala.util.Random

object ScafiIncarnationForAlchemist extends BasicAbstractIncarnation
  with StandardLibrary with BasicTimeAbstraction with BasicSpatialAbstraction {
  override type P = Point3D
  override implicit val idBounded = Builtins.Bounded.of_i

  val LSNS_NODE_MANAGER = "manager"
  val LSNS_ENVIRONMENT = "environment"
  val LSNS_COORDINATES = "coordinates"
  val LSNS_RANDOM_ALCHEMIST = "alchemistRandomGen"

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
    def node = sense[NodeManager](LSNS_NODE_MANAGER)

    def coordinates = sense[DoubleArray](LSNS_COORDINATES)

    def currTime: Double = sense[it.unibo.alchemist.model.interfaces.Time](LSNS_TIME).toDouble()

    def dt(whenNan: Double = Double.NaN): Double = {
      val dt = sense[it.unibo.alchemist.model.interfaces.Time](LSNS_DELTA_TIME).toDouble()
      if(dt.isNaN) whenNan else dt
    }

    //def nextRandom: Double = sense[RandomGenerator](LSNS_RANDOM).nextDouble()
    def alchemistRandomGen = sense[RandomGenerator](LSNS_RANDOM_ALCHEMIST)
    lazy val randomGen: Random = new AlchemistRandomWrapper(sense[RandomGenerator](LSNS_RANDOM))
    override def randomGenerator(): Random = randomGen

    def environment = sense[Environment[Any,Position[_]]](LSNS_ENVIRONMENT)
  }
}
