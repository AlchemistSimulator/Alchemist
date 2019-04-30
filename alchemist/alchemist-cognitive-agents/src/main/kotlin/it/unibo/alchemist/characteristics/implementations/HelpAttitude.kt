package it.unibo.alchemist.characteristics.implementations

import it.unibo.alchemist.agents.Pedestrian
import it.unibo.alchemist.characteristics.interfaces.RelationalCharacteristic
import it.unibo.alchemist.model.interfaces.Position

class HelpAttitude() : RelationalCharacteristic {

    fun <T, P : Position<P>> level(ped: Pedestrian<T, P>): Double = TODO()
}