/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.impact

import it.unibo.alchemist.model.cognitive.CognitiveModel
import it.unibo.alchemist.model.cognitive.impact.cognitive.BeliefDanger
import it.unibo.alchemist.model.cognitive.impact.cognitive.CognitiveCharacteristic
import it.unibo.alchemist.model.cognitive.impact.cognitive.DesireEvacuate
import it.unibo.alchemist.model.cognitive.impact.cognitive.DesireWalkRandomly
import it.unibo.alchemist.model.cognitive.impact.cognitive.Fear
import it.unibo.alchemist.model.cognitive.impact.cognitive.IntentionEvacuate
import it.unibo.alchemist.model.cognitive.impact.cognitive.IntentionWalkRandomly
import kotlin.reflect.KClass

/**
 * Path to the file containing characteristics parameters.
 */
const val PARAMETERS_FILE = "it/unibo/alchemist/model/cognitive/impact/config.toml"

/**
 * Agent-based evacuation model with social contagion mechanisms.
 * More information can be found [here](https://doi.org/10.1007/978-3-319-70647-4_11).
 */
class ImpactModel(
    compliance: Double,
    influencedBy: () -> List<CognitiveModel>,
    environmentalFactors: () -> Double,
) : CognitiveModel {

    private val cognitiveCharacteristics = linkedMapOf<KClass<out CognitiveCharacteristic>, CognitiveCharacteristic>(
        BeliefDanger::class to
            BeliefDanger(environmentalFactors, { characteristicLevel<Fear>() }, influencedBy),
        Fear::class to Fear(
            { characteristicLevel<DesireWalkRandomly>() },
            { characteristicLevel<DesireEvacuate>() },
            influencedBy,
        ),
        DesireEvacuate::class to DesireEvacuate(
            compliance,
            { characteristicLevel<BeliefDanger>() },
            { characteristicLevel<Fear>() },
        ),
        DesireWalkRandomly::class to DesireWalkRandomly(
            compliance,
            { characteristicLevel<BeliefDanger>() },
            { characteristicLevel<Fear>() },
        ),
        IntentionEvacuate::class to IntentionEvacuate(
            { characteristicLevel<DesireWalkRandomly>() },
            { characteristicLevel<DesireEvacuate>() },
        ),
        IntentionWalkRandomly::class to IntentionWalkRandomly(
            { characteristicLevel<DesireWalkRandomly>() },
            { characteristicLevel<DesireEvacuate>() },
        ),
    )

    override fun dangerBelief() = characteristicLevel<BeliefDanger>()

    override fun fear() = characteristicLevel<Fear>()

    override fun remainIntention(): Double = characteristicLevel<IntentionWalkRandomly>()

    override fun escapeIntention(): Double = characteristicLevel<IntentionEvacuate>()

    override fun update(frequency: Double) =
        cognitiveCharacteristics.values.forEach { it.update(frequency) }

    private inline fun <reified C : CognitiveCharacteristic> characteristicLevel(): Double =
        cognitiveCharacteristics[C::class]?.level() ?: 0.0
}
