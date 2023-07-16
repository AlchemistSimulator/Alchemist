/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.keybind

import it.unibo.alchemist.boundary.fxui.interaction.keyboard.ActionFromKey
import javafx.scene.input.KeyCode
import tornadofx.getProperty
import tornadofx.property

/**
 * A class that describes the relation between a KeyCode and an Action.
 */
class Keybind(action: ActionFromKey, key: KeyCode) {
    /**
     * The action.
     */
    var action: ActionFromKey by property(action)

    /**
     * The property of the action.
     */
    val actionProperty = getProperty(Keybind::action)

    /**
     * The key.
     */
    var key: KeyCode by property(key)

    /**
     * The property of the key.
     */
    val keyProperty = getProperty(Keybind::key)
}
