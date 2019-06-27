package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender

/**
 * An heterogeneous pedestrian is a pedestrian where individual characteristics are taken into consideration.
 */
interface HeterogeneousPedestrian<T> : Pedestrian<T> {

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
    fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>): Double

    companion object {
        val CHILD_KEYWORDS = setOf("child", "CHILD")
        val ADULT_KEYWORDS = setOf("adult", "ADULT")
        val ELDERLY_KEYWORDS = setOf("elderly", "ELDERLY")

        val MALE_KEYWORDS = setOf("male", "m", "MALE", "M")
        val FEMALE_KEYWORDS = setOf("female", "f", "FEMALE", "F")
    }
}