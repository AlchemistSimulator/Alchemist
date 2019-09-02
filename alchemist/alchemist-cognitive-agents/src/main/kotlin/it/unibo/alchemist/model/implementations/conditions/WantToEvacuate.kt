package it.unibo.alchemist.model.implementations.conditions

import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Context

/**
 * The intention of the pedestrian to evacuate or not.
 */
open class WantToEvacuate<T>(
    private val pedestrian: CognitivePedestrian<T>
) : AbstractCondition<T>(pedestrian) {

    override fun getContext(): Context = Context.LOCAL

    override fun getPropensityContribution(): Double = 0.0

    override fun isValid(): Boolean = pedestrian.wantsToEvacuate()
}