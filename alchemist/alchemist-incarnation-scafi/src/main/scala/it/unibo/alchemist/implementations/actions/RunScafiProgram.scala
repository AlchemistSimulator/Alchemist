/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.implementations.actions

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import it.unibo.alchemist.model.interfaces.Dependency
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.Environment
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.scala.PimpMyAlchemist._
import org.apache.commons.math3.util.FastMath
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist
import ScafiIncarnationForAlchemist.ContextImpl
import ScafiIncarnationForAlchemist._
import it.unibo.alchemist.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.model.implementations.actions.AbstractLocalAction
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.scafi.space.Point3D
import org.kaikikm.threadresloader.ResourceLoader

import scala.concurrent.duration.FiniteDuration

sealed class RunScafiProgram[T,P <: Position[P]] (
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
    this(environment, node, reaction, rng, programName, FastMath.nextUp(reaction.getTimeDistribution.getRate))
  }

  import RunScafiProgram.NBRData
  val program = ResourceLoader.classForName(programName).newInstance().asInstanceOf[CONTEXT => EXPORT]
  val programNameMolecule = new SimpleMolecule(programName)
  private var nbrData: Map[ID, NBRData[P]] = Map()
  private var completed = false

  declareDependencyTo(Dependency.EVERY_MOLECULE)

  def asMolecule = programNameMolecule

  override def cloneAction(n: Node[T], r: Reaction[T]) = {
    new RunScafiProgram(environment, n, r, rng, programName, retentionTime)
  }

  override def execute() {
    import collection.JavaConverters.mapAsScalaMapConverter
    implicit def euclideanToPoint(p: P): Point3D = p.getDimensions match {
      case 1 => Point3D(p.getCoordinate(0), 0, 0)
      case 2 => Point3D(p.getCoordinate(0), p.getCoordinate(1), 0)
      case 3 => Point3D(p.getCoordinate(0), p.getCoordinate(1), p.getCoordinate(2))
    }

    val position: P = environment.getPosition(node)
    val currentTime = reaction.getTau
    if(!nbrData.contains(node.getId)) nbrData += node.getId -> new NBRData(factory.emptyExport(), environment.getPosition(node), Double.NaN)
    nbrData = nbrData.filter { case (id,data) => id==node.getId || data.executionTime >= currentTime - retentionTime }
    val deltaTime = currentTime.minus(nbrData.get(node.getId).map( _.executionTime).getOrElse(Double.NaN))
    val localSensors = node.getContents().asScala.map({
      case (k, v) => k.getName -> v
    }) ++ Map(
        LSNS_ALCHEMIST_COORDINATES -> position.getCartesianCoordinates,
        LSNS_DELTA_TIME -> FiniteDuration(deltaTime.toInt, TimeUnit.SECONDS),
        LSNS_POSITION -> position,
        LSNS_TIMESTAMP -> currentTime.toLong,
        LSNS_TIME -> LocalDateTime.MIN.plusSeconds(currentTime.toDouble.toInt),
        LSNS_ALCHEMIST_NODE_MANAGER -> new SimpleNodeManager(node),
        LSNS_ALCHEMIST_DELTA_TIME -> deltaTime,
        LSNS_ALCHEMIST_ENVIRONMENT -> environment,
        LSNS_ALCHEMIST_RANDOM -> rng,
        LSNS_ALCHEMIST_TIMESTAMP -> currentTime
    )
    val nbrSensors = Map(
        NBR_LAG -> nbrData.mapValues[FiniteDuration](nbr => FiniteDuration((currentTime - nbr.executionTime).toInt, TimeUnit.SECONDS)),
        /*
         * nbrDelay is estimated: it should be nbr(deltaTime), here we suppose the round frequency
         * is negligibly different between devices.
         */
        NBR_DELAY -> nbrData.mapValues[FiniteDuration](nbr => FiniteDuration((nbr.executionTime + deltaTime - currentTime).toInt, TimeUnit.SECONDS)),
        NBR_RANGE -> nbrData.mapValues[Double](_.position.getDistanceTo(position)),
        NBR_VECTOR -> nbrData.mapValues[Point3D](nbr => position - nbr.position),
        NBR_ALCHEMIST_LAG -> nbrData.mapValues[Double](currentTime - _.executionTime),
        NBR_ALCHEMIST_DELAY -> nbrData.mapValues[Double](nbr => nbr.executionTime + deltaTime - currentTime),
    )
    val nbrRange = nbrData.mapValues { _.position }
    val exports = nbrData.mapValues { _.export }
    val ctx = new ContextImpl(node.getId, exports, localSensors, nbrSensors)
    val computed = program(ctx)
    node.setConcentration(programName, computed.root[T]())
    val toSend = NBRData(computed, position, currentTime)
    nbrData = nbrData + (node.getId -> toSend)

    completed = true
  }

  def sendExport(id: ID, export: NBRData[P]) { nbrData += id -> export }

  def getExport(id: ID): Option[NBRData[P]] = nbrData.get(id)

  def isComputationalCycleComplete: Boolean = completed

  def prepareForComputationalCycle: Unit = { completed = false }

}

object RunScafiProgram {
  case class NBRData[P <: Position[P]](export: EXPORT, position: P, executionTime: Time)
}
