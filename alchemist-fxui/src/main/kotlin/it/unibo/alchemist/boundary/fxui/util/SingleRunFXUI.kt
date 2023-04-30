/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.util

import it.unibo.alchemist.AlchemistExecutionOptions
import it.unibo.alchemist.boundary.fxui.EffectGroup
import it.unibo.alchemist.boundary.fxui.effects.serialization.EffectSerializer
import it.unibo.alchemist.launch.Priority
import it.unibo.alchemist.launch.SimulationLauncher
import it.unibo.alchemist.launch.Validation.Invalid
import it.unibo.alchemist.launch.Validation.OK
import it.unibo.alchemist.loader.Loader
import javafx.embed.swing.JFXPanel
import javafx.stage.Stage
import java.awt.GraphicsEnvironment
import java.io.File

/**
 * Executes simulations locally with a JavaFX UI.
 */
object SingleRunFXUI : SimulationLauncher() {
    private const val DEFAULT_EFFECTS = "it/unibo/alchemist/gui/effects/json/DefaultEffects.json"
    override val name = "Alchemist FXUI graphical simulation"

    override fun additionalValidation(currentOptions: AlchemistExecutionOptions) = with(currentOptions) {
        when {
            headless -> incompatibleWith("headless mode")
            batch -> incompatibleWith("batch mode")
            variables.isNotEmpty() -> incompatibleWith("variable exploration mode")
            distributed != null -> incompatibleWith("distributed execution")
            GraphicsEnvironment.isHeadless() ->
                Invalid("The JVM graphic environment is marked as headless. Cannot show a graphical interface. ")
            graphics != null && fxui -> OK(Priority.High("Graphical effects and FXUI requested, priority shifts up"))
            else -> OK(Priority.Fallback("FXUI not requested, default GUI is Swing"))
        }
    }

    override fun launch(loader: Loader, parameters: AlchemistExecutionOptions) {
        prepareSimulation<Any, Nothing>(loader, parameters, emptyMap<String, Any>()).let { simulation ->
            // fetches default effects if graphics is null, otherwise loads from graphics
            val effects: EffectGroup<Nothing> = parameters.graphics?.let {
                EffectSerializer.effectsFromFile<Nothing>(File(it))
            } ?: EffectSerializer.effectsFromResources(DEFAULT_EFFECTS)
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
}
