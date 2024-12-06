/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
@file:Suppress("DEPRECATION")

package it.unibo.alchemist.boundary.swingui.monitor.impl

import it.unibo.alchemist.boundary.swingui.api.GraphicalOutputMonitor
import it.unibo.alchemist.boundary.swingui.effect.impl.EffectSerializationFactory
import it.unibo.alchemist.boundary.swingui.tape.impl.JEffectsTab
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.maps.MapEnvironment
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.DisplayMode
import java.awt.GraphicsEnvironment
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel

/**
 * Creates a Swing-based graphical interface for the provided [environment].
 * The actual implementation of the [main] display is chosen based on the type of [environment]
 */
@Deprecated("The Swing UI must be replaced by a web UI")
class SwingGUI<T, P : Position2D<P>> private constructor(
    val environment: Environment<T, P>,
    graphicsFile: File?,
    closeOperation: Int,
    failOnHeadless: Boolean,
    private val main: Generic2DDisplay<T, P>,
) : GraphicalOutputMonitor<T, P> by main {
    private val headAttached: Boolean = !GraphicsEnvironment.isHeadless()

    /**
     * Builds a single-use graphical interface.
     *
     * @param environment the simulation for this GUI
     * @param graphicsFile the effects file
     * @param closeOperation the type of close operation for this GUI
     */
    constructor(
        environment: Environment<T, P>,
        graphicsFile: File?,
        failOnHeadless: Boolean = false,
        closeOperation: Int = JFrame.EXIT_ON_CLOSE,
    ) : this(environment, graphicsFile, closeOperation, failOnHeadless, makeSwingComponent(environment))

    /**
     * Builds a single-use graphical interface.
     *
     * @param environment the simulation for this GUI
     * @param graphics the effects file
     * @param closeOperation the type of close operation for this GUI
     */
    @JvmOverloads
    constructor(
        environment: Environment<T, P>,
        graphics: String? = null,
        failOnHeadless: Boolean = false,
        closeOperation: Int = JFrame.EXIT_ON_CLOSE,
    ) : this(environment, graphics?.let { File(it) }, failOnHeadless, closeOperation)

    private val timeStepMonitor: TimeStepMonitor<T, P> = TimeStepMonitor()

    init {
        check(!(failOnHeadless && GraphicsEnvironment.isHeadless())) {
            "Cannot run the Swing GUI in headless mode. If you want the simulator to carry on without the UI, " +
                "pass `failOnHeadless: false` as an output monitor parameter"
        }
        if (headAttached) {
            val effects = JEffectsTab(main, false)
            if (graphicsFile != null) {
                require(graphicsFile.exists()) { "Effects file " + graphicsFile.absolutePath + " does not exist" }
                require(!graphicsFile.isDirectory) {
                    "The effects file at " + graphicsFile.absolutePath + " is a directory, but a file was expected"
                }
                effects.setEffects(EffectSerializationFactory.effectsFromFile(graphicsFile))
            }
            val frame = JFrame("Alchemist Simulator")
            frame.defaultCloseOperation = closeOperation
            val canvas = JPanel()
            frame.contentPane.add(canvas)
            canvas.layout = BorderLayout()
            canvas.add(main)
            /*
             * Upper area
             */
            val upper = JPanel()
            upper.layout = BoxLayout(upper, BoxLayout.X_AXIS)
            canvas.add(upper, BorderLayout.NORTH)
            upper.add(effects)
            upper.add(timeStepMonitor)
            /*
             * Go on screen
             */
            val size =
                GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .screenDevices
                    .map { screen -> screen.displayMode }
                    .minByOrNull { it.area() }
            frame.size = size?.run { Dimension((width * SCALE_FACTOR).toInt(), (height * SCALE_FACTOR).toInt()) }
                ?: Dimension(FALLBACK_X_SIZE, FALLBACK_Y_SIZE)
            frame.isLocationByPlatform = true
            frame.isVisible = true
        } else {
            LoggerFactory.getLogger(SwingGUI::class.java).warn(
                "Swing GUI requested, but this JVM instance is running in headless mode. To fail istead of printing" +
                    "this warning, pass `failOnHeadless: true` as an output monitor parameter",
            )
        }
    }

    override fun initialized(environment: Environment<T, P>) {
        if (headAttached) {
            timeStepMonitor.initialized(environment)
            main.initialized(environment)
        }
    }

    override fun stepDone(
        environment: Environment<T, P>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ) {
        if (headAttached) {
            timeStepMonitor.stepDone(environment, reaction, time, step)
            main.stepDone(environment, reaction, time, step)
        }
    }

    override fun finished(
        environment: Environment<T, P>,
        time: Time,
        step: Long,
    ) {
        if (headAttached) {
            timeStepMonitor.finished(environment, time, step)
            main.finished(environment, time, step)
        }
    }

    private companion object {
        private const val SCALE_FACTOR = 0.8f
        private const val FALLBACK_X_SIZE = 800
        private const val FALLBACK_Y_SIZE = 600

        @Suppress("UNCHECKED_CAST")
        private fun <T, P : Position2D<P>> makeSwingComponent(environment: Environment<T, P>): Generic2DDisplay<T, P> =
            when (environment) {
                is MapEnvironment<*, *, *> -> MapDisplay<T>() as Generic2DDisplay<T, P>
                else -> Generic2DDisplay()
            }

        private fun DisplayMode.area(): Int = width * height
    }
}
