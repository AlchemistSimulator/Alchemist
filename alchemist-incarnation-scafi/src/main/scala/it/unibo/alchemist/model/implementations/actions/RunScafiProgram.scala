/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions

import java.util.concurrent.TimeUnit

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.model.interfaces.{Time => _, _}
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.{ContextImpl, _}
import it.unibo.alchemist.scala.PimpMyAlchemist._
import it.unibo.scafi.space.Point3D
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.util.FastMath
import org.kaikikm.threadresloader.ResourceLoader

import scala.concurrent.duration.FiniteDuration

sealed class DefaultRunScafiProgram[P <: Position[P]](
  environment: Environment[Any, P],
  node: Node[Any],
  reaction: Reaction[Any],
  rng: RandomGenerator,
  programName: String,
  retentionTime: Double
) extends RunScafiProgram[Any,P](environment, node, reaction, rng, programName, retentionTime){

  def this(environment: Environment[Any, P],
           node: Node[Any],
           reaction: Reaction[Any],
           rng: RandomGenerator,
           programName: String) = {
    this(environment, node, reaction, rng, programName, FastMath.nextUp(1.0/reaction.getTimeDistribution.getRate))
  }
}

sealed class RunScafiProgram[T, P <: Position[P]] (
    environment: Environment[T, P],
    node: Node[T],
    reaction: Reaction[T],
    rng: RandomGenerator,
    programName: String,
    retentionTime: Double
) extends AbstractLocalAction[T](node) {

  def this(environment: Environment[T, P],
    node: Node[T],
    reaction: Reaction[T],
    rng: RandomGenerator,
    programName: String) = {
    this(environment, node, reaction, rng, programName, FastMath.nextUp(1.0/reaction.getTimeDistribution.getRate))
  }

  import RunScafiProgram.NBRData
  val program = ResourceLoader.classForName(programName).getDeclaredConstructor().newInstance().asInstanceOf[CONTEXT => EXPORT]
  val programNameMolecule = new SimpleMolecule(programName)
  lazy val nodeManager = new SimpleNodeManager(node)
  private var nbrData: Map[ID, NBRData[P]] = Map()
  private var completed = false
  declareDependencyTo(Dependency.EVERY_MOLECULE)

  def asMolecule = programNameMolecule

  override def cloneAction(n: Node[T], r: Reaction[T]) = {
    new RunScafiProgram(environment, n, r, rng, programName, retentionTime)
  }

  override def execute(): Unit = {
    import scala.jdk.CollectionConverters._
    implicit def euclideanToPoint(p: P): Point3D = p.getDimensions match {
      case 1 => Point3D(p.getCoordinate(0), 0, 0)
      case 2 => Point3D(p.getCoordinate(0), p.getCoordinate(1), 0)
      case 3 => Point3D(p.getCoordinate(0), p.getCoordinate(1), p.getCoordinate(2))
    }
    val position: P = environment.getPosition(node)
    // NB: We assume it.unibo.alchemist.model.interfaces.Time = DoubleTime
    //     and that its "time unit" is seconds, and then we get NANOSECONDS
    val alchemistCurrentTime = environment.getSimulation.getTime
    def alchemistTimeToNanos(time: Time): Long = (time.toDouble * 1_000_000_000).toLong
    val currentTime: Long = alchemistTimeToNanos(alchemistCurrentTime)
    if(!nbrData.contains(node.getId)) {
      nbrData += node.getId -> NBRData(factory.emptyExport(), position, Double.NaN)
    }
    nbrData = nbrData.filter { case (id,data) => id==node.getId || data.executionTime >= alchemistCurrentTime - retentionTime }
    val deltaTime: Long = currentTime - nbrData.get(node.getId).map(d => alchemistTimeToNanos(d.executionTime)).getOrElse(0L)
    val localSensors = node.getContents().asScala.map({
      case (k, v) => k.getName -> v
    }) ++ Map(
        LSNS_ALCHEMIST_COORDINATES -> position.getCoordinates,
        LSNS_DELTA_TIME -> FiniteDuration(deltaTime, TimeUnit.NANOSECONDS),
        LSNS_POSITION -> position,
        LSNS_TIMESTAMP -> currentTime,
        LSNS_TIME -> java.time.Instant.ofEpochMilli((alchemistCurrentTime * 1000).toLong),
        LSNS_ALCHEMIST_NODE_MANAGER -> nodeManager,
        LSNS_ALCHEMIST_DELTA_TIME -> alchemistCurrentTime.minus(nbrData.get(node.getId).map(_.executionTime).getOrElse(Time.INFINITY)),
        LSNS_ALCHEMIST_ENVIRONMENT -> environment,
        LSNS_ALCHEMIST_RANDOM -> rng,
        LSNS_ALCHEMIST_TIMESTAMP -> alchemistCurrentTime
    )

    val nbrSensors = Map(
      // NBR_LAG captures "how long ago info from neighbours was received"
      NBR_LAG -> nbrData.view.mapValues[FiniteDuration](nbr => FiniteDuration(alchemistTimeToNanos(alchemistCurrentTime - nbr.executionTime), TimeUnit.NANOSECONDS)),
      /*
       * nbrDelay is estimated: it should be nbr(deltaTime), here we suppose the round frequency
       * is negligibly different between devices.
       */
      NBR_DELAY -> nbrData.view.mapValues[FiniteDuration](nbr => FiniteDuration(alchemistTimeToNanos(nbr.executionTime) + deltaTime - currentTime, TimeUnit.NANOSECONDS)),
      NBR_RANGE -> nbrData.view.mapValues[Double](_.position.distanceTo(position)),
      NBR_VECTOR -> nbrData.view.mapValues[Point3D](nbr => position.minus(nbr.position.getCoordinates) ),
      NBR_ALCHEMIST_LAG -> nbrData.view.mapValues[Double](alchemistCurrentTime - _.executionTime),
      NBR_ALCHEMIST_DELAY -> nbrData.view.mapValues[Double](nbr => alchemistTimeToNanos(nbr.executionTime) + deltaTime - currentTime),
    )
    val exports: Iterable[(ID,EXPORT)] = nbrData.view.mapValues { _.export }
    val ctx = new ContextImpl(node.getId, exports, localSensors, Map.empty){
      override def nbrSense[T](nsns: NSNS)(nbr: ID): Option[T] =
        nbrSensors.get(nsns).flatMap(_.get(nbr)).map(_.asInstanceOf[T])
    }
    val computed = program(ctx)
    node.setConcentration(programName, computed.root[T]())
    val toSend = NBRData(computed, position, alchemistCurrentTime)
    nbrData = nbrData + (node.getId -> toSend)
    completed = true
  }

  def sendExport(id: ID, export: NBRData[P]): Unit = { nbrData += id -> export }

  def getExport(id: ID): Option[NBRData[P]] = nbrData.get(id)

  def isComputationalCycleComplete: Boolean = completed

  def prepareForComputationalCycle: Unit = { completed = false }

}

object RunScafiProgram {
  case class NBRData[P <: Position[P]](export: EXPORT, position: P, executionTime: Time)
}
