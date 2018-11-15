package it.unibo.alchemist.input

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javafx.scene.input.KeyCode
import java.io.FileReader

/**
 * Actions which can be bound to a key on the keyboard
 */
enum class ActionFromKey {
    MODIFIER_CONTROL,
    MODIFIER_SHIFT,
    MOVE,
    DELETE,
    EDIT,
    PLAY_AND_PAUSE,
    ONE_STEP,
    PAN_NORTH,
    PAN_SOUTH,
    PAN_EAST,
    PAN_WEST
}

/**
 * Reads and writes a configuration of key bindings to a json file.
 * TODO: write the write function
 */
class Keybinds {
    companion object {
        private val gson: Gson = Gson()
        private const val filename: String = "keys.json"
        private val typeToken: TypeToken<Map<ActionFromKey, Set<KeyCode>>> =
            object : TypeToken<Map<ActionFromKey, Set<KeyCode>>>() {}
        private var config: Map<ActionFromKey, Set<KeyCode>> =
            try {
                // try reading from the default file
                FileReader(filename).let {
                    val tmp: Map<ActionFromKey, Set<KeyCode>> = gson.fromJson(it, typeToken.type)
                    it.close()
                    tmp
                }
            } catch (e: Exception) {
                // build a config with default key binds if the default file does not exist or is not json compliant
                HashMap<ActionFromKey, Set<KeyCode>>().apply {
                    this[ActionFromKey.MODIFIER_CONTROL] = setOf(KeyCode.CONTROL)
                    this[ActionFromKey.MODIFIER_SHIFT] = setOf(KeyCode.SHIFT)
                    this[ActionFromKey.MOVE] = setOf(KeyCode.M)
                    this[ActionFromKey.DELETE] = setOf(KeyCode.DELETE, KeyCode.BACK_SPACE)
                    this[ActionFromKey.PLAY_AND_PAUSE] = setOf(KeyCode.P)
                    this[ActionFromKey.ONE_STEP] = setOf(KeyCode.PERIOD)
                    this[ActionFromKey.PAN_NORTH] = setOf(KeyCode.W)
                    this[ActionFromKey.PAN_SOUTH] = setOf(KeyCode.S)
                    this[ActionFromKey.PAN_EAST] = setOf(KeyCode.D)
                    this[ActionFromKey.PAN_WEST] = setOf(KeyCode.A)
                }
            }

        /**
         * Retrieve the keys bound to a certain action.
         */
        operator fun get(action: ActionFromKey): Set<KeyCode> = config[action] ?: emptySet()

        /**
         * Associate an action with a set of keys.
         */
        operator fun set(action: ActionFromKey, keys: Set<KeyCode>) { config += action to HashSet(keys) }

        /**
         * Write the binds to the file.
         */
        fun write() {
            // TODO
        }
    }
}