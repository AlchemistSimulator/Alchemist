/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.cognitive.impact.individual.Age
import it.unibo.alchemist.model.cognitive.impact.individual.Gender
import it.unibo.alchemist.model.cognitive.impact.individual.HelpAttitude
import it.unibo.alchemist.model.cognitive.impact.individual.Speed
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A capability representing a pedestrian's individual characteristics.
 */
interface HumanProperty<T, S : Vector<S>, A : Transformation<S>> : NodeProperty<T> {

    /**
     * The age of this pedestrian.
     */
    val age: Age

    /**
     * The gender of this pedestrian.
     */
    val gender: Gender

    /**
     * The speed of an agent considering its age, gender and a random factor.
     */
    val speed: Speed

    /**
     * Value between 0 and 1 representing the attitude towards conforming to social rules of this pedestrian.
     */
    val compliance: Double

    /**
     * The attitude of an agent towards helping another agent.
     */
    val helpAttitude: HelpAttitude

    /**
     * Value between 0 and 1 representing the probability this pedestrian will help another pedestrian in difficulty.
     *
     * @param toHelp The pedestrian who needs help.
     */
    fun probabilityOfHelping(toHelp: HeterogeneousPedestrianModel<T, S, A>, isGroupMember: Boolean): Double =
        helpAttitude.level(toHelp.age, toHelp.gender, isGroupMember)
}
