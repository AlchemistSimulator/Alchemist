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
import it.unibo.alchemist.boundary.gui.effects.EffectGroup
import it.unibo.alchemist.boundary.gui.utility.DataFormatFactory
import it.unibo.alchemist.boundary.interfaces.DrawCommand
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor
import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole
import it.unibo.alchemist.input.KeyboardActionListener
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Collectors
import java.util.stream.Stream
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.Pane
import tornadofx.add

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
    protected lateinit var wormhole: BidimensionalWormhole<P>
        private set
    protected val interactions: InteractionManager<T, P> by lazy { InteractionManager(this) }
    private val canvas = Canvas()

    init {
        firstTime = true
        setStep(steps)
        canvas.style = "-fx-background-color: #FFF;"
        canvas.isMouseTransparent = true
        canvas.widthProperty().bind(widthProperty())
        canvas.heightProperty().bind(heightProperty())
        children.add(canvas)
        children.addAll(interactions.canvases)
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
        try {
            if (mayRender.get() && isVisible && !isDisabled) {
                mayRender.set(false)
                Platform.runLater {
                    commandQueue.forEach { it() }
                    mayRender.set(true)
                }
            }
            interactions.repaint()
        } catch (e: UninitializedPropertyAccessException) {
            // wormhole hasn't been initialized
        }
        mutex.release()
    }

    /**
     * Changes the background of the specified [GraphicsContext].
     *
     * @param graphicsContext the graphic component to draw on
     * @param environment the [Environment] that contains the data to pass to [Effects]
     * @return a function of what to do to draw the background
     * @see .repaint
     */
    protected open fun drawBackground(graphicsContext: GraphicsContext, environment: Environment<T, P>): () -> Unit {
        // TODO environment.dimensions is called to avoid the warning, because -werror registers it as an error
        environment.dimensions
        return { graphicsContext.clearRect(0.0, 0.0, width, height) }
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
        stepDone(environment, null, DoubleTime(), 0)
    }

    override fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        if (firstTime) {
            synchronized(this) {
                if (firstTime) {
                    init(environment)
                    update(environment, time)
                }
            }
        } else {
            update(environment, time)
        }
    }

    /**
     * The method initializes everything that is not initializable before first step.
     * Inheriting classes that override this function should always call super.init to ensure proper initialization.
     *
     * @param environment the `Environment`
     */
    protected open fun init(environment: Environment<T, P>) {
        wormhole = Wormhole2D(environment, this)
        wormhole.center()
        wormhole.optimalZoom()
        interactions.setWormhole(wormhole)
        firstTime = false
        System.currentTimeMillis()
    }

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
//            environment.simulation.schedule {
//                environment.moveNodeToPosition(environment.getNodeByID(0), LatLongPosition(8, 8))
//            }
        if (Thread.holdsLock(environment)) {
            time.toDouble()
            interactions.environment = environment
            /*
             * TODO: Future optimization -- Let the simulation (or the environment, probably both)
             * expose the last moment at which a change in position occurred. This way, we don't
             * need to constantly regenerate the position map.
             */
            interactions.nodes = environment.nodes.associate { Pair(it, environment.getPosition(it)) }
            val graphicsContext = canvas.graphicsContext2D
            val background = Stream.of(drawBackground(graphicsContext, environment))
            val effects = effects
                .stream()
                .map<Queue<DrawCommand<P>>> { group -> group.computeDrawCommands(environment) }
                .flatMap<DrawCommand<P>> { it.stream() }
                .map { cmd -> { cmd.accept(graphicsContext, wormhole) } }
            commandQueue = Stream
                .concat(background, effects)
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
        const val TIME_STEP = (1 / DEFAULT_FRAME_RATE).toDouble()
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
}
