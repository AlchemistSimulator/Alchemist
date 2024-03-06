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

/**
 * Class containing common properties to the whole UI.
 *
 * */
class CommonProperties {

    /**
     * Object containing properties related to the canvas context.
     */
    object RenderProperties {
        /**
         * Number of iterations for scaling calculations.
         */
        private const val SCALE_ITERATIONS = 100

        /**
         * Default radius for nodes.
         */
        const val DEFUALT_NODE_RADIUS = 10.0

        /**
         * Default height of the canvas.
         */
        const val DEFAULT_HEIGHT = 875.0

        /**
         * Default width of the canvas.
         */
        const val DEFAULT_WIDTH = 1350.0 // 1375.0

        /**
         * Default scale factor for rendering.
         */
        const val DEFAULT_SCALE = 13.0

        /**
         * Default starting position for rendering.
         */
        const val DEFAULT_START_POSITION = 0

        /**
         * Default ratio for scaling.
         */
        const val DEFAULT_SCALE_RATIO = 0.9

        /**
         * Minimum scale allowed.
         */
        const val MIN_SCALE = 1.0

        /**
         * Maximum scale allowed.
         */
        const val MAX_SCALE = MIN_SCALE * (1 / DEFAULT_SCALE_RATIO * SCALE_ITERATIONS)

        /**
         * Default color for nodes.
         */
        const val DEFAULT_NODE_COLOR = "#0E835C" // "#0F4AA2"
    }

    /**
     * Object containing observable properties.
     */
    object Observables {
        /**
         * Redux store for managing scale and translation.
         */
        val scaleTranslationStore = createTypedReduxStore(::scaleTranslateReducer, ScaleTranslateState())

        /**
         * Observable value for node radius.
         */
        var nodesRadius: ObservableValue<Double> = ObservableValue(RenderProperties.DEFUALT_NODE_RADIUS)
    }

    /**
     * Utility methods.
     */
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
