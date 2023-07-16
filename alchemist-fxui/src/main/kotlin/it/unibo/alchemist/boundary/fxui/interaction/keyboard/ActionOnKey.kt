/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.keyboard

import it.unibo.alchemist.boundary.fxui.interaction.TriggerAction
import javafx.scene.input.KeyCode

/**
 * The [TriggerAction] objects regarding key strokes.
 */
enum class ActionOnKey {
    PRESSED,
    RELEASED,
    ;

    /**
     * Returns a [KeyboardTriggerAction] initialized with this [ActionOnKey] and the given [KeyCode].
     *
     * @param [key] the [KeyCode] used to initialize the [KeyboardTriggerAction].
     */
    infix fun with(key: KeyCode): KeyboardTriggerAction = KeyboardTriggerAction(this, key)
}
