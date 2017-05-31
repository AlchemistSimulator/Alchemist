package test.scafiprograms

import it.unibo.alchemist.implementation.nodes.NodeManager
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

trait ScafiAlchemistSupport { self: AggregateProgram =>
  def env = sense[NodeManager]("manager")
}

class ScafiGradientProgram extends AggregateProgram {
  override def main(): Double = gradient(sense[Boolean]("source"))

  def gradient(source: Boolean): Double =
    rep(Double.PositiveInfinity){
      distance => mux(source) { 0.0 } {
        foldhood(Double.PositiveInfinity)(Math.min)(nbr{distance}+nbrvar[Double](NBR_RANGE_NAME))
      }
    }
}

class ScafiEnvProgram extends AggregateProgram with ScafiAlchemistSupport {
  override def main(): Any = {
    env.put("number2", env.get[Int]("number")+100)
  }
}

object MyMain extends App {
  val program = new ScafiGradientProgram()
  program.round(new ContextImpl(1, Map(), Map("source"->true), Map("nbrRange" -> Map(1 -> 0))))
}