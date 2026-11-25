/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.impact.cognitive

import kotlinx.serialization.Serializable

/**
 * The parameters of the cognitive characteristics.
 *
 * @property sensingOmega ability to perceive danger.
 * @property affectiveBiasingOmega influence of fear on the perception of danger.
 * @property persistingOmega how long fear persists in time.
 * @property amplifyingFeelingOmega amplifies fear.
 * @property inhibitingFeelingOmega inhibits fear.
 * @property amplifyingEvacuationOmega amplifies the desire to flee from danger.
 * @property inhibitingWalkRandOmega reduces the desire to flee from danger.
 * @property amplifyingIntentionOmega increases the intention to flee from danger.
 * @property inhibitingIntentionOmega reduces the intention to flee from danger.
 * @property mentalEta speed of mental changes.
 * @property bodyEta speed of body changes.
 * @property logisticSigma σ of sig_{στ} in the IMPACT model.
 * @property logisticTau τ of sig_{στ} in the IMPACT model.
 * @property advancedLogisticSigma σ of asig_{στ} in the IMPACT model.
 * @property advancedLogisticTau τ of asig_{στ} in the IMPACT model.
 */
@Serializable
internal data class CognitiveParameters(
    val sensingOmega: Double,
    val affectiveBiasingOmega: Double,
    val persistingOmega: Double,
    val amplifyingFeelingOmega: Double,
    val inhibitingFeelingOmega: Double,
    val amplifyingEvacuationOmega: Double,
    val inhibitingWalkRandOmega: Double,
    val amplifyingIntentionOmega: Double,
    val inhibitingIntentionOmega: Double,
    val mentalEta: Double,
    val bodyEta: Double,
    val logisticSigma: Double,
    val logisticTau: Double,
    val advancedLogisticSigma: Double,
    val advancedLogisticTau: Double,
)
