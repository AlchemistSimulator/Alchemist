package it.unibo.alchemist.scala

import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule

object PimpMyAlchemist {
  implicit class RichPosition(position: Position) {
    def -(p: Position) = position.subtract(p)
    def +(p: Position) = position.add(p)
  }
  implicit val zeroTime = DoubleTime.ZERO_TIME
  implicit def time2Double(time: Time): Double = time.toDouble()
  implicit def double2Time(time: Double): Time = new DoubleTime(time)
  implicit def molecule2String(molecule: Molecule): String = molecule.toString
  implicit def string2Molecule(str: String): Molecule = new SimpleMolecule(str)
}
