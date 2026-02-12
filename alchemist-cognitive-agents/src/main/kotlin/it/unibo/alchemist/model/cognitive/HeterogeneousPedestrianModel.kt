/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.cognitive.impact.individual.Age
import it.unibo.alchemist.model.cognitive.impact.individual.Compliance
import it.unibo.alchemist.model.cognitive.impact.individual.Gender
import it.unibo.alchemist.model.cognitive.impact.individual.HelpAttitude
import it.unibo.alchemist.model.cognitive.impact.individual.Speed
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * Model of a pedestrian with heterogeneous individual characteristics.
 *
 * @param T the concentration type.
 * @param S the concrete Vector type used for geometry operations.
 * @param A the concrete Transformation type compatible with [S].
 */
data class HeterogeneousPedestrianModel<T, S : Vector<S>, A : Transformation<S>>(
    /** The age of this pedestrian. */
    val age: Age,
    /** The gender of this pedestrian. */
    val gender: Gender,
    /** The agent's speed considering age, gender and randomness. */
    val speed: Speed,
    /**
     * Value between 0 and 1 representing the attitude toward conforming to social rules.
     */
    val compliance: Double = Compliance(age, gender).level,
    /** The attitude toward helping other agents. */
    val helpAttitude: HelpAttitude = HelpAttitude(age, gender),
) {
    /**
     * Returns the probability that this pedestrian will help another pedestrian.
     *
     * @param toHelp the pedestrian who may receive help.
     * @param isGroupMember true if the pedestrian in difficulty is a member of the helper's group.
     * @return a probability in [0.0, 1.0] representing the likelihood of offering help.
     */
    fun probabilityOfHelping(toHelp: HeterogeneousPedestrianModel<T, S, A>, isGroupMember: Boolean): Double =
        helpAttitude.level(toHelp.age, toHelp.gender, isGroupMember)
}
