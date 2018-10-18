/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.scafi

import it.unibo.alchemist.implementation.nodes.NodeManager
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.scafi.PlatformDependentConstants
import it.unibo.scafi.incarnations.BasicAbstractIncarnation
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point3D}
import it.unibo.scafi.time.BasicTimeAbstraction

object ScafiIncarnationForAlchemist extends BasicAbstractIncarnation
  with StandardLibrary with BasicTimeAbstraction with BasicSpatialAbstraction {
  override type P = Point3D
  override implicit val idBounded = Builtins.Bounded.of_i

  val LSNS_NODE_MANAGER = "manager"
  val LSNS_ENVIRONMENT = "environment"
  val LSNS_COORDINATES = "coordinates"
  val LSNS_RANDOM_VALUE = "random"

  trait ScafiAlchemistSupport { self: AggregateProgram with StandardSensors =>
    def node = sense[NodeManager](LSNS_NODE_MANAGER)
    def currTime: Double = sense[it.unibo.alchemist.model.interfaces.Time](LSNS_TIME).toDouble()
    def dt(whenNan: Double = Double.NaN): Double = {
      val dt = sense[it.unibo.alchemist.model.interfaces.Time](LSNS_DELTA_TIME).toDouble()
      if(dt.isNaN) whenNan else dt
    }
    def nextRandom: Double = sense[()=>java.lang.Double](LSNS_RANDOM_VALUE)().toDouble
    def environment = sense[Environment[Any]](LSNS_ENVIRONMENT)

    override def aggregate[T](f: => T): T =
      vm.nest(FunCall[T](vm.index, sun.reflect.Reflection.getCallerClass(PlatformDependentConstants.StackTracePosition).getName()))(!vm.neighbour.isDefined) {
        f
      }
  }
}
