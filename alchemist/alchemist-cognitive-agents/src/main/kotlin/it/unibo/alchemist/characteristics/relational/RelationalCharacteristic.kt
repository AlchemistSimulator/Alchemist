package it.unibo.alchemist.characteristics.relational

import it.unibo.alchemist.agents.Pedestrian
import it.unibo.alchemist.characteristics.Characteristic
import it.unibo.alchemist.model.interfaces.Position

/**
 * A characteristic which depends also on the other pedestrians in the environment
 */
abstract class RelationalCharacteristic<T, P : Position<P>> : Characteristic {

    protected lateinit var owner: Pedestrian<T, P>

    fun ownership(ped: Pedestrian<T, P>) { owner = ped }
}