/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.keybind

import it.unibo.alchemist.boundary.fxui.keybind.ListKeybindsView.Companion.SPACING_SMALL
import javafx.beans.property.StringProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import tornadofx.View
import tornadofx.get
import tornadofx.keyboard
import tornadofx.label
import tornadofx.paddingAll
import tornadofx.toProperty
import tornadofx.vbox
import java.util.ResourceBundle

/**
 * The view through which keybinds can be edited.
 */
class EditKeybindView : View() {
    private val toEdit: KeybindModel by inject()

    init {
        if (messages.baseBundleName == null) {
            messages = ResourceBundle.getBundle("it.unibo.alchemist.l10n.KeybinderStrings")
        }
    }

    /**
     * {@inheritDoc}.
     */
    override val titleProperty: StringProperty
        get() = messages["title_edit_keybind"].toProperty()

    /**
     * {@inheritDoc}.
     */
    override val root = vbox(SPACING_SMALL) {
        label(
            "${messages["label_key_rebind"]} ${toEdit.actionProperty.value}. " +
                "${messages["label_key_current"]}: ${toEdit.keyProperty.value}",
        )
        keyboard {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code != KeyCode.ESCAPE) {
                    toEdit.item.key = it.code
                }
                close()
            }
        }
        paddingAll = SPACING_SMALL
    }
}
