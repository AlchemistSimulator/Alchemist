/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.util

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.fxui.EffectGroup
import it.unibo.alchemist.boundary.fxui.effects.serialization.EffectSerializer
import it.unibo.alchemist.boundary.launch.SimulationLauncher
import javafx.embed.swing.JFXPanel
import javafx.stage.Stage
import java.io.File

/**
 * Executes simulations locally with a JavaFX UI.
 */
class SingleRunFXUI(
    private val graphics: String? = defaultEffects,
) : SimulationLauncher() {

    override fun launch(loader: Loader) {
        prepareSimulation<Any, Nothing>(loader, emptyMap<String, Any>()).let { simulation ->
            // fetches default effects if graphics is null, otherwise loads from graphics
            val effects: EffectGroup<Nothing> = graphics?.let {
                EffectSerializer.effectsFromFile(File(it))
            } ?: EffectSerializer.effectsFromResources(defaultEffects)
            // launches the JavaFX application thread
            JFXPanel()
            // runs the UI
            JavaFXThreadUtil.runOnFXThread {
                SingleRunApp<Any, Nothing>().apply {
                    setEffectGroups(listOf(effects))
                    setSimulation(simulation)
                }.start(Stage())
            }
            // runs the simulation
            simulation.run()
        }
    }

    companion object {
        /**
         * Default visual effects.
         */
        const val defaultEffects = "it/unibo/alchemist/gui/effects/json/DefaultEffects.json"
    }
}
