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

/**
 * Represents actions related to scaling and translation in the application.
 * This sealed class defines different types of actions as its subclasses.
 */
sealed class ScaleTranslateAction : RAction {

    /**
     * Action to set the scale value.
     * @param scale The new scale value to set.
     */
    data class SetScale(val scale: Double) : ScaleTranslateAction()

    /**
     * Action to set the translation values.
     * @param translate The new translation pair to set, representing the (x, y) translation.
     */
    data class SetTranslation(var translate: Pair<Double, Double>) : ScaleTranslateAction()
}
