/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("DEPRECATION")

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.swingui.monitor.impl.SwingGUI
import org.kaikikm.threadresloader.ResourceLoader

class TestLoadSwingGUI : StringSpec(
    {
        "the Swing-based GUI should be loadable" {
            val loader = LoadAlchemist.from(ResourceLoader.getResource("test.yml"))
            loader.getDefault<Any, Nothing>().outputMonitors.first { it is SwingGUI }
        }
    },
)
