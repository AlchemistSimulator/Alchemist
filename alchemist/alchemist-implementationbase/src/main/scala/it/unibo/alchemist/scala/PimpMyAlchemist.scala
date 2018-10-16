/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.scala

import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import com.google.common.cache.CacheLoader
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

object PimpMyAlchemist {
  /**
   * Wraps a Position, providing + and - operations.
   */
  @SuppressFBWarnings(Array("NM_METHOD_NAMING_CONVENTION"))
  implicit class RichPosition[P <: Position[P]](position: P) {
    def -(p: P) = position.minus(p)
    def +(p: P) = position.plus(p)
  }
  /** Shortcut for DoubleTime.ZERO_TIME */
  implicit val zeroTime = DoubleTime.ZERO_TIME
  implicit def time2Double(time: Time): Double = time.toDouble()
  implicit def double2Time(time: Double): Time = new DoubleTime(time)
  implicit def molecule2String(molecule: Molecule): String = molecule.toString
  implicit def string2Molecule(str: String): Molecule = new SimpleMolecule(str)
  implicit def function2CacheLoader[F, T](f: F => T) = { new CacheLoader[F, T] { def load(key: F) = f(key) } }
}
