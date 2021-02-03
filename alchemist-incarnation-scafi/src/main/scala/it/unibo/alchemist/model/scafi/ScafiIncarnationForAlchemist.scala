/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.scafi

import java.util.Optional

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes.NodeManager
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.{Environment, Layer, Position}
import it.unibo.scafi.incarnations.BasicAbstractIncarnation
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point2D, Point3D}
import it.unibo.scafi.time.BasicTimeAbstraction
import org.apache.commons.math3.random.RandomGenerator

import scala.util.{Random, Success, Try}

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

    def alchemistRandomGen = sense[RandomGenerator](LSNS_ALCHEMIST_RANDOM)
    lazy val randomGen: Random = new AlchemistRandomWrapper(alchemistRandomGen)
    override def randomGenerator(): Random = randomGen
    override def nextRandom(): Double = alchemistRandomGen.nextDouble()

    def alchemistEnvironment = sense[Environment[Any,Position[_]]](LSNS_ALCHEMIST_ENVIRONMENT)

    private implicit def optionalToOption[E](p : Optional[E]) : Option[E] = if (p.isPresent) Some(p.get()) else None

    private def findInLayers[A](name : String) : Option[A] = {
      val layer : Option[Layer[Any, Position[_]]] = alchemistEnvironment.getLayer(new SimpleMolecule(name))
      val node = alchemistEnvironment.getNodeByID(mid())
      layer.map(l => l.getValue(alchemistEnvironment.getPosition(node)))
        .map(value => Try(value.asInstanceOf[A]))
        .collect { case Success(value) => value }
    }

    def senseEnvData[A](name: String): A = {
      findInLayers[A](name).get
    }
  }

  /**
   * Typical adjustment that needs to be performed when using Alchemist environments with positions of type [[Euclidean2DPosition]]
   * to properly adapt values and types to ScaFi standard sensors.
   */
  trait AlchemistEuclidean2DPosition { self: AggregateProgram with ScafiAlchemistSupport with StandardSensors =>
    override def currentPosition(): Point3D = {
      val pos = sense[Euclidean2DPosition](LSNS_POSITION)
      Point3D(pos.getX, pos.getY, 0)
    }

    def current2DPosition(): Point2D = Point2D(currentPosition().x, currentPosition().y)
  }
}
