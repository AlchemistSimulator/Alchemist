package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A pedestrian with individual characteristics.
 */
interface HeterogeneousPedestrian<T, S : Vector<S>, A : GeometricTransformation<S>> : Pedestrian<T, S, A> {

    /**
     * The age of this pedestrian.
     */
    val age: Age

    /**
     * The gender of this pedestrian.
     */
    val gender: Gender

    /**
     * Value between 0 and 1 representing the attitude towards conforming to social rules of this pedestrian.
     */
    val compliance: Double

    /**
     * Value between 0 and 1 representing the probability this pedestrian will help another pedestrian in difficulty.
     *
     * @param toHelp The pedestrian who needs help.
     */
    fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T, S, A>): Double
}
