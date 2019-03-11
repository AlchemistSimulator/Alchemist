/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model

import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.implementations.actions.RunScafiProgram
import java.util.Objects

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.implementation.nodes.ScafiNode
import it.unibo.alchemist.model.implementations.reactions.Event
import it.unibo.alchemist.model.implementations.timedistributions.{DiracComb, ExponentialTime}
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.scala.ScalaInterpreter
import com.google.common.cache.CacheBuilder
import it.unibo.alchemist.model.interfaces.Position

sealed class ScafiIncarnation[P <: Position[P]] extends Incarnation[Any, P]{


  private[this] def notNull[T](t: T): T = Objects.requireNonNull(t)

  private[this] def toDouble(v: Any): Double = v match {
    case x: Double => x
    case x: Int => x
    case x: String => java.lang.Double.parseDouble(x)
    case x: Boolean => if (x) 1 else 0
    case x: Long => x
    case x: Float => x
    case x: Byte => x
    case x: Short => x
    case _ => Double.NaN
  }

  override def createAction(
      rand: RandomGenerator,
      env: Environment[Any, P],
      node: Node[Any],
      time: TimeDistribution[Any],
      reaction: Reaction[Any],
      param: String) = {
    new RunScafiProgram(notNull(env), notNull(node), notNull(reaction), notNull(rand), notNull(param))
  }

  /**
   * NOTE: String v may be prefixed by "_" symbol to avoid caching the value resulting from its interpretation
   */
  override def createConcentration(v: String) = {
    /*
     * TODO: support double-try parse in case of strings (to avoid "\"string\"" in the YAML file)
     */
    val doCacheValue = !v.startsWith("_");
    CachedInterpreter[AnyRef](if(doCacheValue) v else v.tail, doCacheValue)
  }

  override def createCondition(rand: RandomGenerator, env: Environment[Any, P] , node: Node[Any], time: TimeDistribution[Any], reaction: Reaction[Any], param: String) = {
    throw new UnsupportedOperationException("Use the type/parameters syntax to initialize conditions.")
  }

  override def createMolecule(s: String ): SimpleMolecule = {
    new SimpleMolecule(notNull(s))
  }

  override def createNode(rand: RandomGenerator, env: Environment[Any, P], param: String) = {
    new ScafiNode(env)
  }

  override def createReaction(rand: RandomGenerator, env: Environment[Any, P], node: Node[Any], time: TimeDistribution[Any], param: String) = {
    new Event(node, time)
  }

  override def createTimeDistribution(rand: RandomGenerator, env: Environment[Any, P], node: Node[Any], param: String): TimeDistribution[Any] = {
    Objects.requireNonNull(param)
    val frequency = toDouble(param)
    if (frequency.isNaN()) {
      throw new IllegalArgumentException(param + " is not a valid number, the time distribution could not be created.")
    }
    new DiracComb(new DoubleTime(rand.nextDouble() / frequency), frequency);
  }

  override def getProperty(node: Node[Any], molecule: Molecule, propertyName: String) = {
    val target = node.getConcentration(molecule)
    if (propertyName == null || propertyName.trim.isEmpty) {
      toDouble(target)
    } else {
      toDouble(ScalaInterpreter("val value = " + target + ";" + propertyName))
    }
  }
}

object CachedInterpreter {

  import scalacache._
  import guava._
  import com.google.common.cache.CacheBuilder
  import scalacache.ScalaCache
  import scalacache.guava.GuavaCache
  private val underlyingGuavaCache = CacheBuilder.newBuilder()
    .maximumSize(1000L)
    .build[String, Object]
  private implicit val scalaCache = ScalaCache(GuavaCache(underlyingGuavaCache))

  /**
   * Evaluates str using Scala reflection.
   * When doCacheValue is true, the result of the evaluation is cached.
   * When doCacheValue is false, only the result of parsing is cached.
   */
  def apply[A <: AnyRef](str: String, doCacheValue: Boolean = true): A =
    (if(doCacheValue) {
      sync.caching("//VAL"+str)(ScalaInterpreter[A](str))
    } else {
      ScalaInterpreter(str)
    }).asInstanceOf[A]
}
