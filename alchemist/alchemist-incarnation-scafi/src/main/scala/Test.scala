import it.unibo.scafi.incarnations.BasicAbstractIncarnation

object ScafiIncarnationForAlchemist  extends BasicAbstractIncarnation {
}

import ScafiIncarnationForAlchemist.factory
import ScafiIncarnationForAlchemist.AggregateProgram
import ScafiIncarnationForAlchemist.NBR_RANGE_NAME
import ScafiIncarnationForAlchemist.ContextImpl

object MyAggregateProgram extends AggregateProgram {
  def gradient(source: Boolean): Double =
    rep(Double.PositiveInfinity){
      println("traaaaaaaaaaaaaiaaaaa")
      distance => mux(source) { 0.0 } {
        foldhood(Double.MaxValue)(Math.min)(nbr{distance}+nbrvar[Double](NBR_RANGE_NAME))
      }
    }

  def isSource = sense[Boolean]("source")

  override def main() = gradient(isSource)
}


object MyTest extends App {
 println(MyAggregateProgram.round(new ContextImpl(0, Map(), Map("source" -> true), Map("nbrRange" -> Map(0 -> 0d)))))
}
