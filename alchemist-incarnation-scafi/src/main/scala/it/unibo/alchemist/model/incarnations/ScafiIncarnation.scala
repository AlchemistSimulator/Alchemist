/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.incarnations

import it.unibo.alchemist.model.*
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.{ChemicalReaction, Event}
import it.unibo.alchemist.model.scafi.actions.{RunScafiProgram, SendScafiMessage}
import it.unibo.alchemist.model.scafi.conditions.ScafiComputationalRoundComplete
import it.unibo.alchemist.model.scafi.properties.ScafiDevice
import it.unibo.alchemist.model.timedistributions.{DiracComb, ExponentialTime}
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.scala.ScalaInterpreter
import org.apache.commons.math3.random.RandomGenerator

import java.util.Objects
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

sealed class ScafiIncarnation[T, P <: Position[P]] extends Incarnation[T, P]:

  import ScafiIncarnationUtils.runInScafiDeviceContext

  private def notNull[V](value: V, name: String = "Object"): V =
    Objects.requireNonNull(value, s"$name must not be null")

  private def toDouble(value: Any): Double = value match
    case x: Double => x
    case x: Int => x
    case x: String => java.lang.Double.parseDouble(x)
    case x: Boolean => if x then 1 else 0
    case x: Long => x.toDouble
    case x: Float => x
    case x: Byte => x
    case x: Short => x
    case _ => Double.NaN

  override def createAction(
      randomGenerator: RandomGenerator,
      environment: Environment[T, P],
      node: Node[T],
      reaction: Actionable[T],
      param: Any
  ): Action[T] = runInScafiDeviceContext[T, Action[T]](
    node,
    message = s"The node must have a ${classOf[ScafiDevice[?]].getSimpleName} property",
    body = device =>
      if param == "send" then
        val alreadyConfigured = ScafiIncarnationUtils
          .allActions[T, P, SendScafiMessage[T, P]](node, classOf[SendScafiMessage[T, P]])
          .map(_.program)
        val scafiProgramsList = ScafiIncarnationUtils.allScafiProgramsFor[T, P](node)
        scafiProgramsList --= alreadyConfigured
        if scafiProgramsList.isEmpty then
          throw new IllegalStateException(
            "There is no program requiring a " + classOf[SendScafiMessage[T, P]].getSimpleName + " action"
          )
        if scafiProgramsList.size > 1 then
          throw new IllegalStateException(
            "There are too many programs requiring a " + classOf[
              SendScafiMessage[T, P]
            ].getName + " action: " + scafiProgramsList
          )
        new SendScafiMessage[T, P](environment, device, reaction.asInstanceOf[Reaction[T]], scafiProgramsList.head)
      else
        require(param != null, "Unsupported program: null")
        val action = new RunScafiProgram[T, P](
          notNull(environment, "environment"),
          notNull(node, "node"),
          notNull(reaction.asInstanceOf[Reaction[T]], "reaction"),
          notNull(randomGenerator, "random generator"),
          notNull(param.toString, "action parameter")
        )
        // Trigger validation immediately
        device.validateCommunicationConfiguration()
        action
  )

  /** NOTE: String v may be prefixed by the "_" symbol to avoid caching the value resulting from its interpretation */
  override def createConcentration(data: Any): T =
    /*
     * TODO: support double-try parse in case of strings (to avoid "\"string\"" in the YAML file)
     */
    val dataString = data.toString
    val doCacheValue = !dataString.startsWith("_")
    CachedInterpreter[AnyRef](if doCacheValue then dataString else dataString.tail, doCacheValue).asInstanceOf[T]

  override def createConcentration(): T = null.asInstanceOf[T]

  override def createCondition(
      randomGenerator: RandomGenerator,
      environment: Environment[T, P],
      node: Node[T],
      reaction: Actionable[T],
      parameters: Any
  ): Condition[T] = runInScafiDeviceContext[T, Condition[T]](
    node,
    message = s"The node must have a ${classOf[ScafiDevice[?]].getSimpleName} property",
    device =>
      val alreadyConfgiured = ScafiIncarnationUtils
        .allConditionsFor(node, classOf[ScafiComputationalRoundComplete[T]])
        .map(_.asInstanceOf[ScafiComputationalRoundComplete[T]])
        .map(_.program.asInstanceOf[RunScafiProgram[T, P]])
      val scafiProgramList: mutable.Buffer[RunScafiProgram[T, P]] = ScafiIncarnationUtils.allScafiProgramsFor(node)
      scafiProgramList --= alreadyConfgiured
      if scafiProgramList.isEmpty then
        throw new IllegalStateException(
          "There is no program requiring a " +
            classOf[ScafiComputationalRoundComplete[?]].getSimpleName + " condition"
        )
      if scafiProgramList.size > 1 then
        throw new IllegalStateException(
          "There are too many programs requiring a " +
            classOf[ScafiComputationalRoundComplete[?]].getName + " condition: " + scafiProgramList
        )
      new ScafiComputationalRoundComplete(device, scafiProgramList.head).asInstanceOf[Condition[T]]
  )

  override def createMolecule(value: String): SimpleMolecule =
    new SimpleMolecule(notNull(value, "simple molecule name"))

  override def createNode(
      randomGenerator: RandomGenerator,
      environment: Environment[T, P],
      parameters: Any
  ): GenericNode[T] =
    val scafiNode = new GenericNode[T](environment)
    scafiNode.addProperty(new ScafiDevice(scafiNode))
    scafiNode

  override def createReaction(
      randomGenerator: RandomGenerator,
      environment: Environment[T, P],
      node: Node[T],
      time: TimeDistribution[T],
      parameters: Any
  ): Reaction[T] =
    val parameterString = Option(parameters).map(_.toString).orNull
    val isSend = "send".equalsIgnoreCase(parameterString)
    val result: Reaction[T] =
      if isSend then
        new ChemicalReaction[T](
          Objects.requireNonNull[Node[T]](node),
          Objects.requireNonNull[TimeDistribution[T]](time)
        )
      else new Event[T](node, time)
    if parameters != null then
      result.setActions(
        ListBuffer[Action[T]](createAction(randomGenerator, environment, node, result, parameterString)).asJava
      )
    if isSend then
      result.setConditions(
        ListBuffer[Condition[T]](createCondition(randomGenerator, environment, node, result, null)).asJava
      )
    result

  override def createTimeDistribution(
      randomGenerator: RandomGenerator,
      environment: Environment[T, P],
      node: Node[T],
      parameters: Any
  ): TimeDistribution[T] =
    if parameters == null then return new ExponentialTime[T](Double.PositiveInfinity, randomGenerator)
    val frequency = toDouble(parameters)
    if frequency.isNaN then
      throw new IllegalArgumentException(
        parameters.toString + " is not a valid number, the time distribution could not be created."
      )
    new DiracComb(new DoubleTime(randomGenerator.nextDouble() / frequency), frequency)

  override def getProperty(node: Node[T], molecule: Molecule, propertyName: String): Double =
    val target = node.getConcentration(molecule)
    if propertyName == null || propertyName.trim.isEmpty then toDouble(target)
    else toDouble(ScalaInterpreter("val value = " + target + ";" + propertyName))

object ScafiIncarnationUtils:
  def runInScafiDeviceContext[T, A](node: Node[T], message: String, body: ScafiDevice[T] => A): A =
    if !isScafiNode(node) then throw new IllegalArgumentException(message)
    body(node.asProperty(classOf[ScafiDevice[T]]))

  private def isScafiNode[T](node: Node[T]): Boolean =
    node.asPropertyOrNull[ScafiDevice[T]](classOf[ScafiDevice[T]]) != null

  def allActions[T, P <: Position[P], C](node: Node[T], klass: Class[C]): mutable.Buffer[C] =
    for
      reaction: Reaction[T] <- node.getReactions.asScala
      action: Action[T] <- reaction.getActions.asScala if klass.isInstance(action)
    yield action.asInstanceOf[C]

  def allScafiProgramsFor[T, P <: Position[P]](node: Node[T]): mutable.Buffer[RunScafiProgram[T, P]] =
    allActions[T, P, RunScafiProgram[T, P]](node, classOf[RunScafiProgram[T, P]])

  def allConditionsFor[T](node: Node[T], conditionClass: Class[?]): mutable.Buffer[Condition[T]] =
    for
      reaction <- node.getReactions.asScala
      condition <- reaction.getConditions.asScala if conditionClass.isInstance(condition)
    yield condition

object CachedInterpreter:

  import com.google.common.cache.{Cache, CacheBuilder}

  // Values are wrapped in Option, as Guava caches reject nulls
  private val cache: Cache[String, Option[AnyRef]] =
    CacheBuilder.newBuilder().maximumSize(1000L).build[String, Option[AnyRef]]()

  /**
   * Evaluates str using Scala reflection. When doCacheValue is true, the result of the evaluation is cached. When
   * doCacheValue is false, only the result of parsing is cached.
   */
  def apply[A <: AnyRef](code: String, doCacheValue: Boolean = true): A =
    if doCacheValue then cache.get(code, () => Option(ScalaInterpreter[AnyRef](code))).orNull.asInstanceOf[A]
    else ScalaInterpreter(code)
