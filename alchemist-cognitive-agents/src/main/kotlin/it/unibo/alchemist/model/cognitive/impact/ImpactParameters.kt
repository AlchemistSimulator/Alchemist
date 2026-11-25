/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.impact

import com.akuleshov7.ktoml.Toml
import it.unibo.alchemist.model.cognitive.impact.cognitive.CognitiveParameters
import it.unibo.alchemist.model.cognitive.impact.individual.ComplianceParameters
import it.unibo.alchemist.model.cognitive.impact.individual.HelpAttitudeParameters
import it.unibo.alchemist.model.cognitive.impact.individual.SpeedParameters
import kotlinx.serialization.Serializable

/**
 * The parameters of the IMPACT model, as written in [PARAMETERS_FILE].
 */
@Serializable
internal data class ImpactParameters(
    val cognitive: CognitiveParameters,
    val speed: SpeedParameters,
    val compliance: ComplianceParameters,
    val helpAttitude: HelpAttitudeParameters,
) {
    companion object {
        /**
         * The parameters read from [PARAMETERS_FILE].
         */
        val default: ImpactParameters = Toml.decodeFromString(
            serializer(),
            checkNotNull(ImpactParameters::class.java.classLoader.getResource(PARAMETERS_FILE)) {
                "Missing $PARAMETERS_FILE from the classpath"
            }.readText(),
        )
    }
}
