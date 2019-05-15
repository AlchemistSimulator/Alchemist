package it.unibo.alchemist.agents.heterogeneous

import it.unibo.alchemist.agents.homogeneous.Pedestrian
import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Compliance
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.characteristics.individual.Speed

/**
 * An heterogeneous pedestrian is a pedestrian where individual characteristics are taken into consideration.
 */
interface HeterogeneousPedestrian<T> : Pedestrian<T> {

    val age: Age

    val gender: Gender

    val speed: Speed

    val compliance: Compliance

    fun probabilityOfHelping(toHelp: HeterogeneousPedestrian<T>): Double
}