package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.CognitiveModel
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A heterogeneous pedestrian with cognitive capabilities.
 */
interface CognitivePedestrian<T, V, A> : Pedestrian<T, V, A> where
V : Vector<V>,
A : GeometricTransformation<V> {
    /**
     * The mind model of this pedestrian.
     */
    val cognitiveModel: CognitiveModel

    /**
     * The mind model of all people considered influencial for this cognitive pedestrian.
     */
    fun influencialPeople(): List<CognitiveModel> = senses.flatMap { (type, sense) -> sense }
        .filterIsInstance<CognitivePedestrian<T, V, A>>()
        .map { it.cognitiveModel }
}
