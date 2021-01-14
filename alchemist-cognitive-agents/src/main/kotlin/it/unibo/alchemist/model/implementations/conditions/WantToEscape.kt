package it.unibo.alchemist.model.implementations.conditions

import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * The intention of the pedestrian to evacuate or not.
 */
open class WantToEscape<T, S : Vector<S>, A : GeometricTransformation<S>>(
    private val pedestrian: CognitivePedestrian<T, S, A>
) : AbstractCondition<T>(pedestrian) {

    override fun getContext(): Context = Context.LOCAL

    override fun getPropensityContribution(): Double = 0.0

    override fun isValid(): Boolean = pedestrian.wantsToEscape()
}
