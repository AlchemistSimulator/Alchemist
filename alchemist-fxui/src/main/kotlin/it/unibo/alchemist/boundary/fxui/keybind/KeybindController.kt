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
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.Keybinds
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.input.KeyCode
import tornadofx.Controller

/**
 * The controller for ListKeybindsView.
 */
class KeybindController : Controller() {
    /**
     * The current keybinds.
     */
    val keybinds: ObservableList<Keybind> = FXCollections.observableList(
        Keybinds.config.asSequence()
            .map { Keybind(it.key, it.value) }
            .plus(ActionFromKey.values().map { Keybind(it, KeyCode.UNDEFINED) })
            .distinctBy { it.action }.toList(),
    )

    /**
     * The keybind currently selected in the view.
     */
    val selected = KeybindModel()
}
