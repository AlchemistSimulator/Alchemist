package it.unibo.alchemist.boundary

import it.unibo.alchemist.input.ActionFromKey
import it.unibo.alchemist.input.Keybinds
import java.util.ResourceBundle
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

/**
 * A class that describes the relation between a KeyCode and an Action
 */
class Keybind(action: ActionFromKey, key: KeyCode) {
    /**
     * The action
     */
    var action: ActionFromKey by property(action)
    /**
     * The property of the action
     */
    val actionProperty = getProperty(Keybind::action)

    /**
     * The key
     */
    var key: KeyCode by property(key)
    /**
     * The property of the key
     */
    val keyProperty = getProperty(Keybind::key)
}

/**
 * The ItemViewModel of a Keybind
 */
class KeybindModel : ItemViewModel<Keybind>() {
    /**
     * The property of the action
     */
    val actionProperty: Property<ActionFromKey> = bind(Keybind::actionProperty)
    /**
     * The property of the key
     */
    val keyProperty: Property<KeyCode> = bind(Keybind::keyProperty)
}

/**
 * The controller for ListKeybindsView
 */
class KeybindController : Controller() {
    /**
     * The current keybinds
     */
    val keybinds: ObservableList<Keybind> = FXCollections.observableList(
        Keybinds.config.asSequence()
            .map { Keybind(it.key, it.value) }
            .plus(ActionFromKey.values().map { Keybind(it, KeyCode.UNDEFINED) })
            .distinctBy { it.action }.toList()
        )
    /**
     * The keybind currently selected in the view
     */
    val selected = KeybindModel()
}

/**
 * The view that lists current keybinds
 */
class ListKeybindsView : View() {

    /**
     * The controller
     */
    val controller: KeybindController by inject()

    /**
     * {@inheritDoc}
     */
    override val titleProperty: StringProperty
        get() = messages["title_keybinds_list"].toProperty()
    /**
     * {@inheritDoc}
     */
    override val root = vbox(10.0) {
        tableview(controller.keybinds) {
            column(messages["column_action"], Keybind::actionProperty).minWidth(200)
            column(messages["column_key"], Keybind::keyProperty).minWidth(150).remainingWidth()
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
            button(messages["button_close"]) {
                action {
                    Keybinds.config = controller.keybinds.associate { it.action to it.key }
                    Keybinds.save()
                    close()
                }
            }
        }
        paddingAll = 10.0
    }
}

/**
 * The view through which keybinds can be edited
 */
class EditKeybindView : View() {
    private val toEdit: KeybindModel by inject()

    /**
     * {@inheritDoc}
     */
    override val titleProperty: StringProperty
        get() = messages["title_edit_keybind"].toProperty()

    /**
     * {@inheritDoc}
     */
    override val root = vbox(10.0) {
        label("${messages["label_key_rebind"]} ${toEdit.actionProperty.value}. " +
            "${messages["label_key_current"]}: ${toEdit.keyProperty.value}")
        keyboard {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code != KeyCode.ESCAPE) {
                    toEdit.item.key = it.code
                }
                close()
            }
        }
        paddingAll = 10.0
    }
}

/**
 * The keybinder app
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
