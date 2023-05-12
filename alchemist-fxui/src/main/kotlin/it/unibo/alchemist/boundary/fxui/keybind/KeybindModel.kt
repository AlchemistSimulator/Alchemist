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
import javafx.beans.property.Property
import javafx.scene.input.KeyCode
import tornadofx.ItemViewModel

/**
 * The ItemViewModel of a Keybind.
 */
class KeybindModel : ItemViewModel<Keybind>() {
    /**
     * The property of the action.
     */
    val actionProperty: Property<ActionFromKey> = bind(Keybind::actionProperty)

    /**
     * The property of the key.
     */
    val keyProperty: Property<KeyCode> = bind(Keybind::keyProperty)
}
