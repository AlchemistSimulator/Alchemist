package it.unibo.alchemist.model

import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.implementation.actions.RunScafiProgram
import java.util.Objects
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.implementation.nodes.ScafiNode
import it.unibo.alchemist.model.implementations.reactions.Event
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.scala.ScalaInterpreter

sealed class ScafiIncarnation extends Incarnation[Any]{


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
      env: Environment[Any],
      node: Node[Any],
      time: TimeDistribution[Any],
      reaction: Reaction[Any],
      param: String) = {
    new RunScafiProgram(notNull(env), notNull(node), notNull(reaction), notNull(rand), notNull(param))
  }

  override def createConcentration(v: String) = {
    /*
     * TODO: support double-try parse in case of strings (to avoid "\"string\"" in the YAML file)
     */
    ScalaInterpreter(v)
  }

  override def createCondition(rand: RandomGenerator, env: Environment[Any] , node: Node[Any], time: TimeDistribution[Any], reaction: Reaction[Any], param: String) = {
    throw new UnsupportedOperationException("Use the type/parameters syntax to initialize conditions.")
  }

  override def createMolecule(s: String ): SimpleMolecule = {
      new SimpleMolecule(notNull(s));
  }

  override def createNode(rand: RandomGenerator, env: Environment[Any], param: String) = {
        new ScafiNode(env)
  }

  override def createReaction(rand: RandomGenerator, env: Environment[Any], node: Node[Any], time: TimeDistribution[Any], param: String) = {
    new Event(node, time)
  }

  override def createTimeDistribution(rand: RandomGenerator, env: Environment[Any], node: Node[Any], param: String) = {
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