/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.keyboard.api

import it.unibo.alchemist.boundary.fxui.interaction.api.ActionListener
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.impl.KeyboardTriggerAction
import javafx.scene.input.KeyEvent

/**
 * An action listener in the context of a keyboard.
 */
interface KeyboardActionListener : ActionListener<KeyboardTriggerAction, KeyEvent>
