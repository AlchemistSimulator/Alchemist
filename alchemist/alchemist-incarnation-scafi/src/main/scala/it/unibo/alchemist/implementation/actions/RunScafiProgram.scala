/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions

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
import ScafiIncarnationForAlchemist.ID
import ScafiIncarnationForAlchemist.EXPORT
import ScafiIncarnationForAlchemist.CONTEXT
import ScafiIncarnationForAlchemist.factory
import it.unibo.alchemist.implementation.nodes.SimpleNodeManager
import org.kaikikm.threadresloader.ResourceLoader

sealed class RunScafiProgram[P <: Position[P]] (
    environment: Environment[Any, P],
    node: Node[Any],
    reaction: Reaction[Any],
    rng: RandomGenerator,
    programName: String,
    retentionTime: Double
    ) extends AbstractLocalAction[Any](node) {

  def this(environment: Environment[Any, P],
    node: Node[Any],
    reaction: Reaction[Any],
    rng: RandomGenerator,
    programName: String) = {
    this(environment, node, reaction, rng, programName, FastMath.nextUp(reaction.getTimeDistribution.getRate))
  }

  import RunScafiProgram.NBRData
  private val program = ResourceLoader.classForName(programName).newInstance().asInstanceOf[CONTEXT => EXPORT]
  private[this] var nbrData: Map[ID, NBRData[P]] = Map()
  addModifiedMolecule(programName)

  override def cloneAction(n: Node[Any], r: Reaction[Any]) = {
    new RunScafiProgram(environment, n, r, rng, programName, retentionTime)
  }

  override def execute() {
    import collection.JavaConverters.mapAsScalaMapConverter
    val position: P = environment.getPosition(node)
    val currentTime = reaction.getTau
    if(!nbrData.contains(node.getId)) nbrData += node.getId -> new NBRData(factory.emptyExport(), environment.getPosition(node), Double.NaN)
    nbrData = nbrData.filter { case (id,data) => id==node.getId || data.executionTime >= currentTime - retentionTime }
    val deltaTime = currentTime.subtract(nbrData.get(node.getId).map( _.executionTime).getOrElse(Double.NaN))
    val localSensors = node.getContents().asScala.map({
      case (k, v) => k.getName -> v
    }) ++ Map(
        "coordinates" -> position.getCartesianCoordinates,
        "dt" -> deltaTime,
        "position" -> position,
        "random" -> {() => rng.nextDouble},
        "time" -> currentTime,
        "manager" -> new SimpleNodeManager(node)
    )
    val nbrSensors = Map(
        "nbrLag" -> nbrData.mapValues[Double](currentTime - _.executionTime),
        /*
         * nbrDelay is estimated: it should be nbr(deltaTime), here we suppose the round frequency
         * is negligibly different between devices.
         */
        "nbrDelay" -> nbrData.mapValues[Double](nbr => nbr.executionTime + deltaTime - currentTime),
        "nbrRange" -> nbrData.mapValues[Double](_.position.getDistanceTo(position)),
        "nbrVector" -> nbrData.mapValues[P](position - _.position)
    )
    val nbrRange = nbrData.mapValues { _.position }
    val exports = nbrData.mapValues { _.export }
    val ctx = new ContextImpl(node.getId, exports, localSensors, nbrSensors)
    val computed = program(ctx)
    node.setConcentration(programName, computed.root[Any]())
    val toSend = NBRData(computed, position, currentTime)
    nbrData = nbrData + (node.getId -> toSend)
    import collection.JavaConverters._
    import it.unibo.alchemist.model.interfaces.Action
    for (nbr: Node[Any] <- environment.getNeighborhood(node).asScala;
        reaction: Reaction[Any] <- nbr.getReactions().asScala;
        action: Action[Any] <- reaction.getActions().asScala;
        if action.isInstanceOf[RunScafiProgram[P]] && action.asInstanceOf[RunScafiProgram[P]].program.getClass == program.getClass) {
      action.asInstanceOf[RunScafiProgram[P]].sendExport(node.getId, toSend)
    }
  }

  private def sendExport(id: ID, export: NBRData[P]) { nbrData += id -> export }
}

object RunScafiProgram {
  private case class NBRData[P <: Position[P]](export: EXPORT, position: P, executionTime: Time)
}
