/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.content.shared

import io.kvision.redux.createTypedReduxStore
import io.kvision.state.ObservableValue
import stores.reducers.scaleTranslateReducer
import stores.states.ScaleTranslateState

class CommonProperties {

    /**
     * Object containing properties related to the canvas context.
     */
    object RenderProperties {
        private const val SCALE_ITERATIONS = 100
        const val DEFUALT_NODE_RADIUS = 10.0
        const val DEFAULT_HEIGHT = 875.0
        const val DEFAULT_WIDTH = 1350.0 // 1375.0
        const val DEFAULT_SCALE = 1.0
        const val DEFAULT_START_POSITION = 0
        const val DEFAULT_SCALE_RATIO = 0.9
        const val MIN_SCALE = 1.0
        const val MAX_SCALE = MIN_SCALE * ((1 / DEFAULT_SCALE_RATIO) * SCALE_ITERATIONS + 1)
        const val DEFAULT_NODE_COLOR = "#0F4AA2"
    }

    /**
     * Object containing observable properties.
     */
    object Observables {
        val scaleTranslationStore = createTypedReduxStore(::scaleTranslateReducer, ScaleTranslateState())
        var nodesRadius: ObservableValue<Double> = ObservableValue(RenderProperties.DEFUALT_NODE_RADIUS)
    }

    object Utils {

        /**
         * Calculates the next scale value based on the specified threshold.
         *
         * @param threshold the threshold value used to determine the direction of scaling
         * @return the next scale value calculated based on the threshold
         */
        fun nextScale(threshold: Double): Double {
            return if (threshold > 0) {
                Observables.scaleTranslationStore.getState().scale * RenderProperties.DEFAULT_SCALE_RATIO
            } else {
                Observables.scaleTranslationStore.getState().scale / RenderProperties.DEFAULT_SCALE_RATIO
            }
        }
    }
}
