/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.boundary.clear
import it.unibo.alchemist.boundary.gui.effects.EffectGroup
import it.unibo.alchemist.boundary.gui.utility.DataFormatFactory
import it.unibo.alchemist.boundary.interactions.InteractionManager
import it.unibo.alchemist.boundary.interfaces.DrawCommand
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor
import it.unibo.alchemist.boundary.jfx.events.keyboard.KeyboardActionListener
import it.unibo.alchemist.boundary.wormhole.implementation.ExponentialZoomManager
import it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.wormhole.implementation.WormholeFX
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Base abstract class for each display able to graphically represent a 2D space and simulation.
 *
 * @param <T> The type which describes the [Concentration] of a molecule
 * @param <P> The type of position
 */
@SuppressFBWarnings(
    "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
    "Field is initialized in the initialize function"
)
abstract class AbstractFXDisplay<T, P : Position2D<P>>
/**
 * Main constructor. It lets the developer specify the number of steps.
 *
 * @param steps the number of steps
 * @see .setStep
 */
@JvmOverloads constructor(steps: Int = DEFAULT_NUMBER_OF_STEPS) : Pane(), FXOutputMonitor<T, P> {

    private val effectStack: ObservableList<EffectGroup<P>> = FXCollections.observableArrayList()
    private val mutex = Semaphore(1)
    private val mayRender = AtomicBoolean(true)
    private var step: Int = 0
    @Volatile private var firstTime: Boolean = false
    private var realTime: Boolean = false
    @Volatile private var commandQueue: ConcurrentLinkedQueue<() -> Unit> = ConcurrentLinkedQueue()
    private var viewStatus = DEFAULT_VIEW_STATUS
    private lateinit var wormhole: Wormhole2D<P>
    private lateinit var zoomManager: ZoomManager
    private val interactions: InteractionManager<T, P> by lazy { InteractionManager(this) }
    private val effectsCanvas = Canvas()
    /**
     * Group dedicated for painting the background.
     */
    protected val background = Group()

    init {
        firstTime = true
        setStep(steps)
        val repaintOnResize = ChangeListener<Number> { _, _, _ ->
            repaint()
        }
        widthProperty().addListener(repaintOnResize)
        heightProperty().addListener(repaintOnResize)
        effectsCanvas.isMouseTransparent = true
        effectsCanvas.widthProperty().bind(widthProperty())
        effectsCanvas.heightProperty().bind(heightProperty())
        children.addAll(background, effectsCanvas, interactions.canvases)
    }

    override fun getViewStatus(): FXOutputMonitor.ViewStatus {
        return this.viewStatus
    }

    override fun setViewStatus(viewStatus: FXOutputMonitor.ViewStatus) {
        this.viewStatus = viewStatus
    }

    override fun getStep(): Int {
        return this.step
    }

    /**
     * {@inheritDoc}.
     *
     * @throws IllegalArgumentException if the step is not bigger than 0
     */
    @Throws(IllegalArgumentException::class)
    override fun setStep(step: Int) {
        if (step <= 0) {
            throw IllegalArgumentException("The parameter must be a positive integer")
        }
        this.step = step
    }

    override fun isRealTime(): Boolean {
        return this.realTime
    }

    override fun setRealTime(realTime: Boolean) {
        this.realTime = realTime
    }

    override fun repaint() {
        mutex.acquireUninterruptibly()
        if (mayRender.get() && isVisible && !isDisabled) {
            mayRender.set(false)
            Platform.runLater {
                commandQueue.forEach { it() }
                mayRender.set(true)
            }
        }
        interactions.repaint()
        mutex.release()
    }

    override fun addEffects(effects: Collection<EffectGroup<P>>) {
        this.effectStack.addAll(effects)
    }

    override fun addEffectGroup(effects: EffectGroup<P>) {
        this.effectStack.add(effects)
    }

    override fun getEffects(): Collection<EffectGroup<P>> {
        return this.effectStack
    }

    override fun setEffects(effects: Collection<EffectGroup<P>>) {
        this.effectStack.clear()
        this.effectStack.addAll(effects)
    }

    override fun getKeyboardListener(): KeyboardActionListener = interactions.keyboardListener

    override fun initialized(environment: Environment<T, P>) {
        init(environment)
        stepDone(environment, null, DoubleTime(), 0)
    }

    override fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        update(environment, time)
    }

    /**
     * The method initializes everything that is not initializable before first step.
     * Inheriting classes that override this function should always call super.init to ensure proper initialization.
     *
     * @param environment the `Environment`
     */
    protected open fun init(environment: Environment<T, P>) {
        wormhole = createWormhole(environment)
        zoomManager = createZoomManager(wormhole)
        interactions.setWormhole(wormhole)
        interactions.setZoomManager(zoomManager)
        wormhole.center()
        wormhole.optimalZoom()
        zoomManager.zoom = wormhole.zoom
        firstTime = false
        System.currentTimeMillis()
    }

    /**
     * Creates a wormhole for this monitor.
     * Subclasses that make use of their own wormholes can set them through this method.
     *
     * @param environment the current environment.
     * @returns the wormhole.
     */
    protected open fun createWormhole(environment: Environment<T, P>): WormholeFX<P> =
        WormholeFX(environment, this)

    /**
     * Creates a zoom manager for this monitor.
     * Subclasess that make use of their own zoom managers can set them through this method.
     *
     * @param wormhole the current wormhole.
     * @returns the zoom manager.
     */
    protected open fun createZoomManager(wormhole: Wormhole2D<P>): ZoomManager =
        ExponentialZoomManager(wormhole.zoom, ExponentialZoomManager.DEF_BASE)

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        update(environment, time)
        firstTime = true
    }

    /**
     * Updates parameter for correct `Environment` representation.
     *
     * @param environment the `Environment`
     * @param time the current `Time` of simulation
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", "False positive")
    private fun update(environment: Environment<T, P>, time: Time) {
        if (Thread.holdsLock(environment)) {
            time.toDouble()
            interactions.environment = environment
//            TODO: Future optimization -- Let the simulation (or the environment, probably both)
//            expose the last moment at which a change in position occurred. This way, we don't
//            need to constantly regenerate the position map.
            interactions.nodes = environment.nodes.associateWith(environment::getPosition)
            val graphicsContext = effectsCanvas.graphicsContext2D
            val clearEffects = Stream.of(effectsCanvas::clear)
            val drawEffects = effects
                .stream()
                .map<Queue<DrawCommand<P>>> { group -> group.computeDrawCommands(environment) }
                .flatMap { it.stream() }
                .map { cmd -> { cmd.accept(graphicsContext, wormhole) } }
            commandQueue = Stream
                .concat(clearEffects, drawEffects)
                .collect(Collectors.toCollection { ConcurrentLinkedQueue<() -> Unit>() })
            interactions.simulationStep()
            repaint()
        } else {
            throw IllegalStateException("Only the simulation thread can dictate GUI updates")
        }
    }

    companion object {
        /**
         * The default frame rate.
         */
        const val DEFAULT_FRAME_RATE: Byte = 60
        /**
         * The default time per frame.
         */
        const val DEFAULT_TIME_STEP = (1 / DEFAULT_FRAME_RATE).toDouble()
        /**
         * Default number of steps.
         */
        const val DEFAULT_NUMBER_OF_STEPS = 1
        /**
         * Position `DataFormat`.
         */
        protected val POSITION_DATA_FORMAT = DataFormatFactory.getDataFormat(Position::class.java)
        /**
         * Default serial version UID.
         */
        private const val serialVersionUID = 1L
        /**
         * The default view status.
         */
        private val DEFAULT_VIEW_STATUS = FXOutputMonitor.ViewStatus.PANNING
    }

    /**
     * @inheritDoc.
     */
    override fun getNode() = this as Node
}
