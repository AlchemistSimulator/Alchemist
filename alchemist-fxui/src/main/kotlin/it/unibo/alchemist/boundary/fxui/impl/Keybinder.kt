/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl

import it.unibo.alchemist.boundary.fxui.interaction.keyboard.util.ActionFromKey
import it.unibo.alchemist.boundary.fxui.interaction.keyboard.util.Keybinds
import javafx.beans.property.Property
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import tornadofx.App
import tornadofx.Controller
import tornadofx.FX
import tornadofx.ItemViewModel
import tornadofx.Scope
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.column
import tornadofx.find
import tornadofx.get
import tornadofx.getProperty
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.keyboard
import tornadofx.label
import tornadofx.launch
import tornadofx.minWidth
import tornadofx.onDoubleClick
import tornadofx.paddingAll
import tornadofx.property
import tornadofx.region
import tornadofx.remainingWidth
import tornadofx.smartResize
import tornadofx.tableview
import tornadofx.toProperty
import tornadofx.vbox
import tornadofx.vgrow
import java.util.ResourceBundle

private const val ACTION_COLUMN_MIN_WIDTH = 200
private const val KEY_COLUMN_MIN_WIDTH = 150
private const val TABLEVIEW_MIN_WIDTH = 400.0
private const val TABLEVIEW_MIN_HEIGHT = 500.0
private const val SPACING_SMALL = 10.0

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
                    find(EditKeybindView::class, it).openModal()
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
}

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
