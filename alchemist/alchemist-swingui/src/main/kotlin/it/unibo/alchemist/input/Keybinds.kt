package it.unibo.alchemist.input

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import javafx.scene.input.KeyCode
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URL

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
 * Reads and writes a configuration of key bindings to a JSON file.
 */
class Keybinds {
    companion object {
        private val filesystemPath = "${System.getProperty("user.home")}${File.separator}Alchemist${File.separator}"
        private val classpathPath = "it${File.pathSeparator}unibo${File.pathSeparator}alchemist${File.pathSeparator}gui${File.pathSeparator}"
        private const val filename: String = "keybinds.json"
        private val typeToken: TypeToken<Map<ActionFromKey, Set<KeyCode>>> =
            object : TypeToken<Map<ActionFromKey, Set<KeyCode>>>() {}
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        private var config: Map<ActionFromKey, Set<KeyCode>> = try {
            // try reading from the file system
            gson.fromJson(URL("$filesystemPath$filename").readText(), typeToken.type)
        } catch (e: Exception) {
            // read from the classpath
            gson.fromJson(
                Keybinds::class.java.classLoader.getResource("$classpathPath$filename").readText(), typeToken.type
            )
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
         * Write the binds to the file system.
         * @throws IOException
         */
        @Throws(IOException::class)
        fun write() {
            File("$filesystemPath$filename").let {
                it.parentFile.mkdirs()
                if (!it.exists()) {
                    it.createNewFile()
                }
                FileWriter(it.path).let { writer ->
                    writer.write(gson.toJson(config))
                    writer.close()
                }
            }
        }
    }
}
