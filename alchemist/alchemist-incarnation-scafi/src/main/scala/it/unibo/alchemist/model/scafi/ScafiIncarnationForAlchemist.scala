package it.unibo.alchemist.model.scafi

import it.unibo.scafi.incarnations.BasicAbstractIncarnation
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point3D}
import it.unibo.scafi.time.BasicTimeAbstraction

object ScafiIncarnationForAlchemist extends BasicAbstractIncarnation
  with StandardLibrary with BasicTimeAbstraction with BasicSpatialAbstraction {
  override type P = Point3D
  override implicit val idBounded = Builtins.Bounded.of_i
}