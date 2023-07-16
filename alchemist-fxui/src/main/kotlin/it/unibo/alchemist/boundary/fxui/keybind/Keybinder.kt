/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.keybind

import tornadofx.App
import tornadofx.FX
import tornadofx.launch
import java.util.ResourceBundle

/**
 * The keybinder app.
 */
class Keybinder : App(ListKeybindsView::class) {
    init {
        FX.messages = ResourceBundle.getBundle("it.unibo.alchemist.l10n.KeybinderStrings")
    }

    companion object {
        /**
         * Function for launching the GUI from java classes.
         */
        fun run() = launch<Keybinder>()
    }
}
