/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.actions

import io.kvision.redux.RAction

sealed class ScaleTranslateAction : RAction {
    data class SetScale(val scale: Double) : ScaleTranslateAction()
    data class SetTranslation(var translate: Pair<Double, Double>) : ScaleTranslateAction()
}
