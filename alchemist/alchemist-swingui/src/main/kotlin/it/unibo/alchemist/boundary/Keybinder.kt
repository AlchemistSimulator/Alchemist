package it.unibo.alchemist.boundary

import it.unibo.alchemist.input.ActionFromKey
import it.unibo.alchemist.input.Keybinds
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.collections.ObservableSet
import javafx.geometry.Insets
import javafx.scene.control.SelectionMode
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.App
import tornadofx.Fragment
import tornadofx.ItemViewModel
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.column
import tornadofx.enableWhen
import tornadofx.getValue
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.launch
import tornadofx.listview
import tornadofx.minWidth
import tornadofx.observable
import tornadofx.onDoubleClick
import tornadofx.region
import tornadofx.remainingWidth
import tornadofx.setValue
import tornadofx.smartResize
import tornadofx.tableview
import tornadofx.toProperty
import tornadofx.vbox
import tornadofx.vgrow
// TODO: use wildcard import?
// ktlint-disable no-wildcard-imports
// import tornadofx.*

class Keybind(action: ActionFromKey, keys: ObservableSet<KeyCode>) {
    val actionProperty = SimpleObjectProperty(this, "action", action)
    var action by actionProperty

    val keysProperty = SimpleSetProperty(this, "keys", keys)
    var keys by keysProperty
}

class KeybindModel : ItemViewModel<Keybind>() {
    val action = bind(Keybind::actionProperty)
    val keys = bind(Keybind::keysProperty)
}

class ListKeybindsView : View("keybinds") {

    private val selected = KeybindModel()

    private val binds = Keybinds
        .config.asSequence().map {
            Keybind(it.key, it.value.observable())
        }.toList().observable()

    override val root = vbox(10.0) {
        tableview(binds) {
            column("ACTION", Keybind::action).minWidth(70)
            column("KEYS", Keybind::keys).minWidth(100).remainingWidth()
            smartResize()
            bindSelected(selected)
            vgrow = Priority.ALWAYS
            onDoubleClick {
                find<EditKeybindFragment>(mapOf(EditKeybindFragment::toEdit to selected)).openWindow()
            }
        }
        hbox(8.0) {
            region {
                hgrow = Priority.ALWAYS
            }
            button("Save and close") {
                action {
                    Keybinds.config = binds.associate { it.action to it.keys }
                    Keybinds.save()
                }
            }
        }
        padding = Insets(10.0)
    }
}

class KeyModel : ItemViewModel<KeyCode>() {
    val key = bind(KeyCode::toProperty)
}

class EditKeybindFragment : Fragment("edit keybind") {

    val toEdit: KeybindModel by param()
    private val selected = KeyModel()
    private val keys = toEdit.keys.value.toList().observable()

    override val root = vbox(10.0) {
        listview(keys) {
            bindSelected(selected)
            selectionModel.selectionMode = SelectionMode.SINGLE
        }
        hbox(8.0) {
            button("Add bind") {
            }
            button("Remove bind") {
            }
            region {
                hgrow = Priority.ALWAYS
            }
            button("Apply") {
                enableWhen(toEdit.dirty)
            }
            button("Cancel") {
            }
        }
        padding = Insets(10.0)
    }
}

class TestApp : App(ListKeybindsView::class)

fun main(args: Array<String>) {
    Keybinds.load()
    launch<TestApp>()
}