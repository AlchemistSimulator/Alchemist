package it.unibo.alchemist.boundary

import it.unibo.alchemist.input.ActionFromKey
import it.unibo.alchemist.input.Keybinds
import javafx.geometry.Insets
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.App
import tornadofx.ItemViewModel
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.column
import tornadofx.getProperty
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.launch
import tornadofx.minWidth
import tornadofx.observable
import tornadofx.property
import tornadofx.region
import tornadofx.remainingWidth
import tornadofx.smartResize
import tornadofx.tableview
import tornadofx.vbox
import tornadofx.vgrow
import java.util.Optional

// TODO: use wildcard import?
// ktlint-disable no-wildcard-imports
// import tornadofx.*

class Keybind(action: ActionFromKey, key: Optional<KeyCode>) {
    var action by property(action)
    fun actionProperty() = getProperty(Keybind::action)

    var key by property(key)
    fun keyProperty() = getProperty(Keybind::key)
}

class KeybindModel : ItemViewModel<Keybind>() {
    val action = bind(Keybind::actionProperty)
    val keys = bind(Keybind::keyProperty)
}

class ListKeybindsView : View("keybinds") {

    private val selected = KeybindModel()

    private val binds = Keybinds
        .map.asSequence().map {
            Keybind(it.key, it.value)
        }.toList().observable()

    override val root = vbox(10.0) {
        tableview(binds) {
            column("ACTION", Keybind::actionProperty).minWidth(200)
            column("KEY", Keybind::keyProperty).minWidth(150).remainingWidth()
            smartResize()
            bindSelected(selected)
            vgrow = Priority.ALWAYS
        }
        hbox(8.0) {
            region {
                hgrow = Priority.ALWAYS
            }
            button("Save and close") {
                action {
                    Keybinds.map = binds.associate { it.action to it.key }
//                    Keybinds.save()
                    println(Keybinds.map)
                    close()
                }
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
