/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package components.content.shared

import components.content.SimulationContext
import io.kvision.redux.createTypedReduxStore
import io.kvision.state.ObservableValue
import stores.reducers.scaleTranslateReducer
import stores.states.ScaleTranslateState

class CommonProperties {

    object RenderProperties {
        const val DEFUALT_NODE_RADIUS = 5.0
        const val DEFAULT_HEIGHT = 900.0
        const val DEFAULT_WIDTH = 1400.0
        const val DEFAULT_SCALE = 25
        const val DEFAULT_START_POSITION = 0
        const val DEFAULT_SCALE_RATIO = 0.8
        const val DEFAULT_NODE_COLOR = "#FF0000"
        const val MIN_SCALE = 1.0
        const val MAX_SCALE = 1111.0
    }

    object Observables{
        val scaleTranslationStore = createTypedReduxStore(::scaleTranslateReducer, ScaleTranslateState())
        var nodesRadius: ObservableValue<Double> = ObservableValue(RenderProperties.DEFUALT_NODE_RADIUS)
    }

    object Utils{
        fun nextScale(threshold: Double): Double{
            return if(threshold > 0)
                Observables.scaleTranslationStore.getState().scale * RenderProperties.DEFAULT_SCALE_RATIO
            else
                Observables.scaleTranslationStore.getState().scale / RenderProperties.DEFAULT_SCALE_RATIO
        }
    }
}