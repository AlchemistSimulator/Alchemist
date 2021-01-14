package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.CognitiveAgent
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A heterogeneous pedestrian with cognitive capabilities.
 */
interface CognitivePedestrian<T, V, A> : HeterogeneousPedestrian<T, V, A>, CognitiveAgent
    where V : Vector<V>,
          A : GeometricTransformation<V>
