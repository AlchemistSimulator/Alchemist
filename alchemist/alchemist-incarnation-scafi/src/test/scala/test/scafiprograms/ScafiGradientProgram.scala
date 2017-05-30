package test.scafiprograms

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class ScafiGradientProgram extends AggregateProgram {
  override def main(): Double = gradient(sense[Boolean]("source"))

  def gradient(source: Boolean): Double =
    rep(Double.PositiveInfinity){
      distance => mux(source) { 0.0 } {
        foldhood(Double.PositiveInfinity)(Math.min)(nbr{distance}+nbrvar[Double](NBR_RANGE_NAME))
      }
    }
}

object MyMain extends App {
  val program = new ScafiGradientProgram()
  program.round(new ContextImpl(1, Map(), Map("source"->true), Map("nbrRange" -> Map(1 -> 0))))
}