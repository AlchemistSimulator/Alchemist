package it.unibo.alchemist.model.cognitiveagents.characteristics.individual

import com.uchuhimo.konf.ConfigSpec

object HelpAttitudeSpec : ConfigSpec() {

    object AdultMale : ConfigSpec() {
        val childMale by required<Pair<Double, Double>>()
        val adultMale by required<Pair<Double, Double>>()
        val elderlyMale by required<Pair<Double, Double>>()
        val childFemale by required<Pair<Double, Double>>()
        val adultFemale by required<Pair<Double, Double>>()
        val elderlyFemale by required<Pair<Double, Double>>()
    }

    object AdultFemale : ConfigSpec() {
        val childMale by required<Pair<Double, Double>>()
        val adultMale by required<Pair<Double, Double>>()
        val elderlyMale by required<Pair<Double, Double>>()
        val childFemale by required<Pair<Double, Double>>()
        val adultFemale by required<Pair<Double, Double>>()
        val elderlyFemale by required<Pair<Double, Double>>()
    }

    object ElderlyMale : ConfigSpec() {
        val childMale by required<Pair<Double, Double>>()
        val adultMale by required<Pair<Double, Double>>()
        val elderlyMale by required<Pair<Double, Double>>()
        val childFemale by required<Pair<Double, Double>>()
        val adultFemale by required<Pair<Double, Double>>()
        val elderlyFemale by required<Pair<Double, Double>>()
    }

    object ElderlyFemale : ConfigSpec() {
        val childMale by required<Pair<Double, Double>>()
        val adultMale by required<Pair<Double, Double>>()
        val elderlyMale by required<Pair<Double, Double>>()
        val childFemale by required<Pair<Double, Double>>()
        val adultFemale by required<Pair<Double, Double>>()
        val elderlyFemale by required<Pair<Double, Double>>()
    }
}