package it.unibo.alchemist.model.cognitiveagents.impact.cognitive

import com.uchuhimo.konf.ConfigSpec

/**
 * A specification of the parameters regarding cognitive characteristics to load from a config file.
 */
object CognitiveSpec : ConfigSpec() {
    val sensingOmega by required<Double>()
    val affectiveBiasingOmega by required<Double>()
    val persistingOmega by required<Double>()
    val amplifyingFeelingOmega by required<Double>()
    val inhibitingFeelingOmega by required<Double>()
    val amplifyingEvacuationOmega by required<Double>()
    val inhibitingWalkRandOmega by required<Double>()
    val amplifyingIntentionOmega by required<Double>()
    val inhibitingIntentionOmega by required<Double>()
    val mentalEta by required<Double>()
    val bodyEta by required<Double>()
    val logisticSigma by required<Double>()
    val logisticTau by required<Double>()
    val advancedLogisticSigma by required<Double>()
    val advancedLogisticTau by required<Double>()
}
