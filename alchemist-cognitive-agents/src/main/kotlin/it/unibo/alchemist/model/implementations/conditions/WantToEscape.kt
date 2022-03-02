package it.unibo.alchemist.model.implementations.conditions

import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty

/**
 * The intention of the pedestrian to evacuate or not.
 */
open class WantToEscape<T, S : Vector<S>, A : GeometricTransformation<S>>(
    private val pedestrian: Node<T>
) : AbstractCondition<T>(pedestrian) {

    override fun getContext(): Context = Context.LOCAL

    override fun getPropensityContribution(): Double = 0.0

    override fun isValid(): Boolean =
        pedestrian.asProperty<T, CognitiveProperty<T>>().cognitiveModel.wantsToEscape()
}
