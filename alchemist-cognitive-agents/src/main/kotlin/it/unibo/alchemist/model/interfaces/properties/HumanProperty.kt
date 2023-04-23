/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.HeterogeneousPedestrianModel
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.cognitiveagents.impact.individual.HelpAttitude
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

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

/**
 * A capability representing a pedestrian's individual characteristics in a 2D space.
 */
typealias Human2DProperty<T> = HumanProperty<T, Euclidean2DPosition, Euclidean2DTransformation>
