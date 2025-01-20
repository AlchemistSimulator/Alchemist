/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.scafi

import it.unibo.alchemist.model
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.positions.Euclidean2DPosition

import java.util.Optional
import it.unibo.alchemist.model.implementations.nodes.NodeManager
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.model.{Environment, Layer}
import it.unibo.scafi.incarnations.BasicAbstractIncarnation
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point2D, Point3D}
import it.unibo.scafi.time.BasicTimeAbstraction
import org.apache.commons.math3.random.RandomGenerator

import scala.language.implicitConversions
import scala.util.Random

object ScafiIncarnationForAlchemist
    extends BasicAbstractIncarnation
    with StandardLibrary
    with BasicTimeAbstraction
    with BasicSpatialAbstraction {
  override type P = Point3D
  implicit override val idBounded: ScafiIncarnationForAlchemist.Builtins.Bounded[Int] = Builtins.Bounded.of_i

  val LSNS_ALCHEMIST_NODE_MANAGER = "manager"
  val LSNS_ALCHEMIST_ENVIRONMENT = "environment"
  val LSNS_ALCHEMIST_COORDINATES = "coordinates"
  val LSNS_ALCHEMIST_RANDOM = "alchemistRandomGen"
  val LSNS_ALCHEMIST_DELTA_TIME = "alchemistDeltaTime"
  val LSNS_ALCHEMIST_TIMESTAMP = "alchemistTimestamp"
  val NBR_ALCHEMIST_DELAY = "alchemistNbrDelay"
  val NBR_ALCHEMIST_LAG = "alchemistNbrLag"

  private class AlchemistRandomWrapper(val randomGenerator: RandomGenerator) extends Random {
    override def nextBoolean(): Boolean = randomGenerator.nextBoolean()
    override def nextDouble(): Double = randomGenerator.nextDouble()
    override def nextInt(): Int = randomGenerator.nextInt()
    override def nextInt(n: Int): Int = randomGenerator.nextInt(n)
    override def nextFloat(): Float = randomGenerator.nextFloat()
    override def nextLong(): Long = randomGenerator.nextLong()
    override def nextGaussian(): Double = randomGenerator.nextGaussian()

    override def clone(): AnyRef = new AlchemistRandomWrapper(randomGenerator)
  }

  trait ScafiAlchemistSupport { self: AggregateProgram with StandardSensors =>
    def node: NodeManager = sense[NodeManager](LSNS_ALCHEMIST_NODE_MANAGER)

    def alchemistCoordinates: Array[Double] = sense[Array[Double]](LSNS_ALCHEMIST_COORDINATES)

    def alchemistDeltaTime(whenNan: Double = Double.NaN): Double = {
      val dt = sense[DoubleTime](LSNS_ALCHEMIST_DELTA_TIME).toDouble
      if (dt.isNaN) whenNan else dt
    }

    def alchemistTimestamp: model.Time = sense[it.unibo.alchemist.model.Time](LSNS_ALCHEMIST_TIMESTAMP)

    def alchemistRandomGen: RandomGenerator = sense[RandomGenerator](LSNS_ALCHEMIST_RANDOM)
    private lazy val randomGen: Random = new AlchemistRandomWrapper(alchemistRandomGen)
    override def randomGenerator(): Random = randomGen
    override def nextRandom(): Double = alchemistRandomGen.nextDouble()

    def alchemistEnvironment: Environment[Any, Position[_]] =
      sense[Environment[Any, Position[_]]](LSNS_ALCHEMIST_ENVIRONMENT)

    implicit private def optionalToOption[E](optional: Optional[E]): Option[E] =
      if (optional.isPresent) Some(optional.get()) else None

    private def findInLayers[A](name: String): A = {
      val layer: Layer[Any, Position[_]] = alchemistEnvironment.getLayer(new SimpleMolecule(name))
      val node = alchemistEnvironment.getNodeByID(mid())
      layer
        .getValue(alchemistEnvironment.getPosition(node))
        .asInstanceOf[A]
    }

    def senseEnvData[A](name: String): A =
      findInLayers[A](name)
  }

  /**
   * Typical adjustment that needs to be performed when using Alchemist environments with positions of type
   * [[Euclidean2DPosition]] to properly adapt values and types to ScaFi standard sensors.
   */
  trait AlchemistEuclidean2DPosition { self: AggregateProgram with ScafiAlchemistSupport with StandardSensors =>
    override def currentPosition(): Point3D = {
      val pos = sense[Euclidean2DPosition](LSNS_POSITION)
      Point3D(pos.getX, pos.getY, 0)
    }

    def current2DPosition(): Point2D = Point2D(currentPosition().x, currentPosition().y)
  }
}
