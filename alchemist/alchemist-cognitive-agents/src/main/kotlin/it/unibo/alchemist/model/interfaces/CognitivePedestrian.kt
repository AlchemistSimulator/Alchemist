package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.CognitiveAgent
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * An heterogeneous pedestrian with cognitive capabilities too.
 */
interface CognitivePedestrian<T, V, A> : HeterogeneousPedestrian<T, V, A>, CognitiveAgent
    where V : Vector<V>, A : GeometricTransformation<V>
