package it.unibo.alchemist.model.implementations.conditions

import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty

/**
 * The intention of the pedestrian to evacuate or not.
 */
open class WantToEscape<T, S : Vector<S>, A : GeometricTransformation<S>>(
    node: Node<T>,
) : AbstractCondition<T>(node) {

    override fun getContext(): Context = Context.LOCAL

    override fun getPropensityContribution(): Double = 0.0

    override fun isValid(): Boolean =
        node.asProperty<T, CognitiveProperty<T>>().cognitiveModel.wantsToEscape()
}
