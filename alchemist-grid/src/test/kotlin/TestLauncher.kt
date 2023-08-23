import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.launch.IgniteServerLauncher
import org.kaikikm.threadresloader.ResourceLoader
import java.net.URL

/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

class TestLauncher : StringSpec({
    "launcher should be correctly selected" {
        val loader = LoadAlchemist.from(CONFIG_FILE)
        val launcher = loader.launcher
        launcher::class shouldBe IgniteServerLauncher::class
    }
}) {
    companion object {
        val CONFIG_FILE: URL = ResourceLoader.getResource("config/server-launcher.yml")
    }
}
