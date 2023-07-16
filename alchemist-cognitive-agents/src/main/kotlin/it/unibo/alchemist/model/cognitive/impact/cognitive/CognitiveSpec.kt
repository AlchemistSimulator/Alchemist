package it.unibo.alchemist.model.cognitive.impact.cognitive

import com.uchuhimo.konf.ConfigSpec

/**
 * A specification of the parameters regarding cognitive characteristics to load from a config file.
 */
object CognitiveSpec : ConfigSpec() {

    /**
     * Ability to perceive danger.
     */
    val sensingOmega by required<Double>()

    /**
     * Influence of fear on the perception of danger.
     */
    val affectiveBiasingOmega by required<Double>()

    /**
     * How long the fear sensation persists in time.
     */
    val persistingOmega by required<Double>()

    /**
     * Amplifies the fear.
     */
    val amplifyingFeelingOmega by required<Double>()

    /**
     * A factor inhibiting fear.
     */
    val inhibitingFeelingOmega by required<Double>()

    /**
     * Amplifies the desire to flee from danger.
     */
    val amplifyingEvacuationOmega by required<Double>()

    /**
     * Reduces the desire to flee from danger.
     */
    val inhibitingWalkRandOmega by required<Double>()

    /**
     * Increases the intention to flee from danger.
     */
    val amplifyingIntentionOmega by required<Double>()

    /**
     * Reduces the intention to flee from danger.
     */
    val inhibitingIntentionOmega by required<Double>()

    /**
     * Mental evolution factor (regulates the speed of cognitive changes).
     */
    val mentalEta by required<Double>()

    /**
     * Body evolution factor (regulates the speed of cognitive changes).
     */
    val bodyEta by required<Double>()

    /**
     * σ value as per sig_{στ} in the IMPACT model. Used for internal computation.
     */
    val logisticSigma by required<Double>()

    /**
     * τ value as per sig_{στ} in the IMPACT model. Used for internal computation.
     */
    val logisticTau by required<Double>()

    /**
     * σ value as per asig_{στ} in the IMPACT model. Used for internal computation.
     */
    val advancedLogisticSigma by required<Double>()

    /**
     * τ value as per asig_{στ} in the IMPACT model. Used for internal computation.
     */
    val advancedLogisticTau by required<Double>()
}
