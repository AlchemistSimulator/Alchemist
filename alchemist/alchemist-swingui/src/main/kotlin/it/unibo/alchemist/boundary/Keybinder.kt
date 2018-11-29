package it.unibo.alchemist.boundary

import it.unibo.alchemist.input.ActionFromKey
import it.unibo.alchemist.input.Keybinds
import javafx.geometry.Insets
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import tornadofx.App
import tornadofx.Controller
import tornadofx.ItemViewModel
import tornadofx.Scope
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.column
import tornadofx.find
import tornadofx.getProperty
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.keyboard
import tornadofx.label
import tornadofx.launch
import tornadofx.minWidth
import tornadofx.observable
import tornadofx.onDoubleClick
import tornadofx.property
import tornadofx.region
import tornadofx.remainingWidth
import tornadofx.smartResize
import tornadofx.tableview
import tornadofx.vbox
import tornadofx.vgrow

// TODO: use wildcard import?
// ktlint-disable no-wildcard-imports
// import tornadofx.*

// TODO: use localized strings

class Keybind(action: ActionFromKey, key: KeyCode) {
    var action by property(action)
    val actionProperty = getProperty(Keybind::action)

    var key by property(key)
    val keyProperty = getProperty(Keybind::key)
}

class KeybindModel : ItemViewModel<Keybind>() {
    val actionProperty = bind(Keybind::actionProperty)
    val keyProperty = bind(Keybind::keyProperty)
}

class KeybindController : Controller() {
    val keybinds = Keybinds
        .config.asSequence().map {
        Keybind(it.key, it.value)
    }.plus(
        ActionFromKey.values().map { Keybind(it, KeyCode.UNDEFINED) }
    ).distinctBy { it.action }.toList().observable()
    val selected = KeybindModel()
}

class ListKeybindsView : View("Keybinds") {

    val controller: KeybindController by inject()

    override val root = vbox(10.0) {
        tableview(controller.keybinds) {
            column("ACTION", Keybind::actionProperty).minWidth(200)
            column("KEY", Keybind::keyProperty).minWidth(150).remainingWidth()
            setMinSize(400.0, 500.0)
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
        hbox(8.0) {
            region {
                hgrow = Priority.ALWAYS
            }
            button("Save and close") {
                action {
                    Keybinds.config = controller.keybinds.associate { it.action to it.key }
                    Keybinds.save()
                    close()
                }
            }
        }
        padding = Insets(10.0)
    }
}

class EditKeybindView : View("Edit keybind") {
    private val toEdit: KeybindModel by inject()

    override val root = vbox(10.0) {
        label("Press a key for ${toEdit.actionProperty.value}. Currently: ${toEdit.keyProperty.value}")
        keyboard {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code != KeyCode.ESCAPE) {
                    toEdit.item.key = it.code
                }
                close()
            }
        }
        padding = Insets(10.0)
    }
}

class Keybinder : App(ListKeybindsView::class)

fun main(args: Array<String>) {
    Keybinds.load()
    launch<Keybinder>()
}
