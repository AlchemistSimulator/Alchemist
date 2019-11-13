package it.unibo.alchemist.input

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import javafx.scene.input.KeyCode
import org.kaikikm.threadresloader.ResourceLoader
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Optional

/**
 * Actions which can be bound to a key on the keyboard
 */
enum class ActionFromKey {
    MODIFIER_CONTROL,
    MODIFIER_SHIFT,
    PAN_NORTH,
    PAN_SOUTH,
    PAN_EAST,
    PAN_WEST,
    DELETE,
    MOVE,
    EDIT,
    PLAY_AND_PAUSE,
    ONE_STEP,
}

/**
 * Reads and writes a configuration of key bindings to a JSON file.
 */
class Keybinds {
    companion object {
        private val filesystemPath = "${System.getProperty("user.home")}${File.separator}.alchemist${File.separator}"
        private val classpathPath = "it${File.separator}unibo${File.separator}alchemist${File.separator}gui${File.separator}"
        private const val filename: String = "keybinds.json"
        private val typeToken: TypeToken<Map<ActionFromKey, KeyCode>> =
            object : TypeToken<Map<ActionFromKey, KeyCode>>() {}
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        /**
         * The currently loaded configuration.
         */
        var config: Map<ActionFromKey, KeyCode> = emptyMap()

        /**
         * Retrieve the keys bound to a certain action.
         */
        operator fun get(action: ActionFromKey): Optional<KeyCode> = Optional.ofNullable(config[action])

        /**
         * Associate an action with a set of keys.
         */
        operator fun set(action: ActionFromKey, key: KeyCode) { config += action to key }

        /**
         * Write the binds to the file system.
         * @throws IOException
         */
        @Throws(IOException::class)
        fun save() {
            File("$filesystemPath$filename").let {
                it.parentFile.mkdirs()
                if (!it.exists()) {
                    it.createNewFile()
                }
                FileWriter(it.path).use { w -> w.write(gson.toJson(config)) }
            }
        }

        /**
         * Load the binds from a file in the file system, reverting to classpath on failure.
         */
        fun load() {
            if (loadFromFile().not()) {
                loadFromClasspath()
            }
        }

        /**
         * Attempt to load the binds from the filesystem.
         */
        private fun loadFromFile(): Boolean =
            try {
                config = gson.fromJson(
                    File("$filesystemPath$filename").readLines().reduce { a, b -> a + b },
                    typeToken.type
                )
                true
            } catch (e: Exception) {
                false
            }

        /**
         * Read the binds from classpath.
         */
        private fun loadFromClasspath() {
            config = gson.fromJson(
                ResourceLoader.getResource("$classpathPath$filename").readText(),
                typeToken.type
            )
        }
    }
}
