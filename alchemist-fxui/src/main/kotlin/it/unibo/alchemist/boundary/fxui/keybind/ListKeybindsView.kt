/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.keybind

import it.unibo.alchemist.boundary.fxui.interaction.keyboard.Keybinds
import javafx.beans.property.StringProperty
import javafx.scene.layout.Priority
import tornadofx.Scope
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.column
import tornadofx.get
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.minWidth
import tornadofx.onDoubleClick
import tornadofx.paddingAll
import tornadofx.region
import tornadofx.remainingWidth
import tornadofx.smartResize
import tornadofx.tableview
import tornadofx.toProperty
import tornadofx.vbox
import tornadofx.vgrow
import java.util.ResourceBundle

/**
 * The view that lists current keybinds.
 */
class ListKeybindsView : View() {

    init {
        if (messages.baseBundleName == null) {
            messages = ResourceBundle.getBundle("it.unibo.alchemist.l10n.KeybinderStrings")
        }
    }

    /**
     * The controller.
     */
    val controller: KeybindController by inject()

    /**
     * {@inheritDoc}.
     */
    override val titleProperty: StringProperty
        get() = messages["title_keybinds_list"].toProperty()

    /**
     * {@inheritDoc}.
     */
    override val root = vbox(SPACING_SMALL) {
        tableview(controller.keybinds) {
            column(messages["column_action"], Keybind::actionProperty).minWidth(ACTION_COLUMN_MIN_WIDTH)
            column(messages["column_key"], Keybind::keyProperty).minWidth(KEY_COLUMN_MIN_WIDTH).remainingWidth()
            setMinSize(TABLEVIEW_MIN_WIDTH, TABLEVIEW_MIN_HEIGHT)
            smartResize()
            bindSelected(controller.selected)
            vgrow = Priority.ALWAYS
            onDoubleClick {
                Scope().let {
                    setInScope(controller.selected, it)
                    tornadofx.find(EditKeybindView::class, it).openModal()
                }
            }
        }
        hbox(SPACING_SMALL) {
            region {
                hgrow = Priority.ALWAYS
            }
            button(messages["button_close"]) {
                action {
                    Keybinds.config = controller.keybinds.associate { it.action to it.key }
                    Keybinds.save()
                    close()
                }
            }
        }
        paddingAll = SPACING_SMALL
    }
    companion object {
        internal const val ACTION_COLUMN_MIN_WIDTH = 200
        internal const val KEY_COLUMN_MIN_WIDTH = 150
        internal const val TABLEVIEW_MIN_WIDTH = 400.0
        internal const val TABLEVIEW_MIN_HEIGHT = 500.0
        internal const val SPACING_SMALL = 10.0
    }
}
