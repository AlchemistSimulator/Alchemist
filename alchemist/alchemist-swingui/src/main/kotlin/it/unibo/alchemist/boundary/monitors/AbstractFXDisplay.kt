/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors

import it.unibo.alchemist.boundary.gui.effects.EffectGroup
import it.unibo.alchemist.boundary.gui.utility.DataFormatFactory
import it.unibo.alchemist.boundary.interfaces.DrawCommand
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor
import it.unibo.alchemist.boundary.monitors.utility.SelectionBox
import it.unibo.alchemist.boundary.wormhole.implementation.Wormhole2D
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.InputEvent
import javafx.scene.input.MouseEvent
import java.awt.Point
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
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
</P></T> */

abstract class AbstractFXDisplay<T>
/**
 * Main constructor. It lets the developer specify the number of steps.
 *
 * @param steps the number of steps
 * @see .setStep
 */
@JvmOverloads constructor(steps: Int = DEFAULT_NUMBER_OF_STEPS) : Canvas(), FXOutputMonitor<T, Position2D<*>> {

    private val effectStack: ObservableList<EffectGroup> = FXCollections.observableArrayList()
    private val mutex = Semaphore(1)
    private val mayRender = AtomicBoolean(true)
    private var step: Int = 0
    protected lateinit var wormhole: BidimensionalWormhole<Position2D<*>>
        private set
    @Volatile private var firstTime: Boolean = false
    private var realTime: Boolean = false
    @Volatile private var commandQueue: ConcurrentLinkedQueue<() -> Unit> = ConcurrentLinkedQueue()
    private var viewStatus = DEFAULT_VIEW_STATUS
    private var selection: SelectionBox<T>? = null
    private var interactions: Canvas? = null
    private lateinit var nodes: Map<Node<T>, Position2D<*>>
    private var panPosition: Position2D<*>? = null

    init {
        firstTime = true // ?
        setStep(steps)
        enableEventReceiving()
        style = "-fx-background-color: #FFF;"
    }

    /**
     * Enables [MouseEvent] receiving by enabling Focus and requesting it.
     */
    private fun enableEventReceiving() {
        isFocusTraversable = true
        isFocused = true
    }

    /**
     * Initializes the mouse interaction to the [Canvas] dedicated to interactions.
     * Should be overridden to implement mouse interaction with the GUI.
     * Called in setInteractionCanvas
     */
    protected open fun initMouseListener() {
        interactions!!.setOnMousePressed {
            when (getViewStatus()) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanInitiated(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelectInitiated(it)
                else -> {
                }
            }
        }
        interactions!!.setOnMouseDragged {
            when (getViewStatus()) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanning(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelecting(it)
                else -> {
                }
            }
        }
        interactions!!.setOnMouseReleased {
            when (getViewStatus()) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanned(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelected(it)
                else -> {
                }
            }
        }
        interactions!!.setOnMouseExited {
            when (getViewStatus()) {
                FXOutputMonitor.ViewStatus.PANNING -> onPanCanceled(it)
                FXOutputMonitor.ViewStatus.SELECTING -> onSelectCanceled(it)
                else -> {
                }
            }
        }
    }

    /**
     * Called when a pan gesture is initiated.
     * @param event the caller
     */
    protected fun onPanInitiated(event: MouseEvent) {
        getEventPosition(event).ifPresent { panPosition = it }
        event.consume()
    }

    /**
     * Called while a pan gesture is in progress and the view is moving.
     * @param event the caller
     */
    protected fun onPanning(event: MouseEvent) {
        if (panPosition != null) {
            getEventPosition(event).ifPresent {
                val currentPoint = wormhole.getViewPoint(it)
                val newPoint = wormhole.getViewPoint(panPosition!!).let { makePoint(
                    (wormhole.viewPosition.getX() + (currentPoint.getX() - it.getX())),
                    (wormhole.viewPosition.getY() + (currentPoint.getY() - it.getY()))) }
                wormhole.viewPosition = newPoint
                panPosition = wormhole.getEnvPoint(currentPoint)
                repaint()
            }
        }
        event.consume()
    }

    /**
     * Called when a pan gesture finishes.
     */
    protected fun onPanned(event: MouseEvent) {
        panPosition = null
        event.consume()
    }

    /**
     * Called when a pan gesture is canceled.
     */
    protected fun onPanCanceled(event: MouseEvent) {
        panPosition = null
        event.consume()
    }

    /**
     * Called when a elements gesture is initiated
     */
    protected fun onSelectInitiated(event: MouseEvent) {
        selection = SelectionBox(makePoint(event.x, event.y), interactions!!.graphicsContext2D, wormhole)
        event.consume()
    }

    /**
     * Called while a elements gesture is in progress and the elements is changing.
     */
    protected fun onSelecting(event: MouseEvent) {
        if (selection != null) {
            selection!!.update(makePoint(event.x, event.y))
        }
        event.consume()
    }

    /**
     * Called when a elements gesture finishes.
     */
    protected fun onSelected(event: MouseEvent) {
        if (selection != null) {
            selection!!.finalize(nodes)
        }
        event.consume()
    }

    /**
     * Called when a select gesture is canceled.
     */
    protected fun onSelectCanceled(event: MouseEvent) {
        if (selection != null) {
            selection!!.clear()
            selection = null
        }
        event.consume()
    }

    override fun getViewStatus(): FXOutputMonitor.ViewStatus {
        return this.viewStatus
    }

    override fun setViewStatus(viewStatus: FXOutputMonitor.ViewStatus) {
        this.viewStatus = viewStatus
    }

    /**
     * The method returns the [Position] in the [Environment] of the given `Event`, if any.
     *
     * @param event the event to check
     * @return the position, if any
     */
    protected fun getEventPosition(event: InputEvent): Optional<Position2D<*>> {
        val wormhole = wormhole
        val methods = event.javaClass.methods
        var getX = Optional.empty<Method>()
        var getY = Optional.empty<Method>()
        for (method in methods) {
            val modifier = method.modifiers
            if (Modifier.isPublic(modifier) && !Modifier.isAbstract(modifier)) {
                val name = method.name
                if (name == GET_X_METHOD_NAME) {
                    getX = Optional.of(method)
                } else if (name == GET_Y_METHOD_NAME) {
                    getY = Optional.of(method)
                }
                if (getX.isPresent && getY.isPresent) {
                    break
                }
            }
        }
        return if (getX.isPresent && getY.isPresent) {
            try {
                val x = getX.get().invoke(event) as Number
                val y = getY.get().invoke(event) as Number
                Optional.of(wormhole.getEnvPoint(makePoint(x, y)))
            } catch (e: Exception) {
                when (e) {
                    is IllegalAccessException,
                    is InvocationTargetException -> Optional.empty<Position2D<*>>()
                    else -> throw e
                }
            }
        } else {
            Optional.empty()
        }
    }

    override fun getStep(): Int {
        return this.step
    }

    /**
     * {@inheritDoc}
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
        } catch (e: UninitializedPropertyAccessException) {
            // wormhole hasn't been initialized
        }
        mutex.release()
    }

    /**
     * Changes the background of the specified `GraphicsContext`.
     *
     * @param graphicsContext the graphic component to draw on
     * @param environment     the `Environment` that contains the data to pass to `Effects`
     * @return a function of what to do to draw the background
     * @see .repaint
     */
    protected fun drawBackground(graphicsContext: GraphicsContext, environment: Environment<T, Position2D<*>>): () -> Unit {
        return { graphicsContext.clearRect(0.0, 0.0, width, height) }
    }

    override fun addEffects(effects: Collection<EffectGroup>) {
        this.effectStack.addAll(effects)
    }

    override fun addEffectGroup(effects: EffectGroup) {
        this.effectStack.add(effects)
    }

    override fun getEffects(): Collection<EffectGroup> {
        return this.effectStack
    }

    override fun setEffects(effects: Collection<EffectGroup>) {
        this.effectStack.clear()
        this.effectStack.addAll(effects)
    }

    protected fun getInteractionCanvas() : Canvas? = interactions

    override fun setInteractionCanvas(canvas: Canvas) {
        if (interactions != null) {
            throw IllegalStateException("Cannot set " + this + "'s interaction canvas: it is already set")
        }
        interactions = canvas
        interactions!!.widthProperty().bind(this.widthProperty())
        interactions!!.heightProperty().bind(this.heightProperty())
        interactions!!.toFront()
        interactions!!.graphicsContext2D.globalAlpha = 0.5
        initMouseListener()
    }

    override fun initialized(environment: Environment<T, Position2D<*>>) {
        stepDone(environment, null, DoubleTime(), 0)
    }

    override fun stepDone(environment: Environment<T, Position2D<*>>, reaction: Reaction<T>?, time: Time, step: Long) {
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
     *
     * @param environment the `Environment`
     */
    protected open fun init(environment: Environment<T, Position2D<*>>) {
        wormhole = Wormhole2D(environment, this)
        wormhole.center()
        wormhole.optimalZoom()
        firstTime = false
        System.currentTimeMillis()
    }

    override fun finished(environment: Environment<T, Position2D<*>>, time: Time, step: Long) {
        update(environment, time)
        firstTime = true
    }

    /**
     * Updates parameter for correct `Environment` representation.
     *
     * @param environment the `Environment`
     * @param time        the current `Time` of simulation
     */
    private fun update(environment: Environment<T, Position2D<*>>, time: Time) {
        if (Thread.holdsLock(environment)) {
            nodes = environment.nodes.associate { Pair(it, environment.getPosition(it)) }
            time.toDouble()
//            environment.simulation.schedule{ environment.moveNodeToPosition(environment.getNodeByID(0), LatLongPosition(8, 8)) }
            val graphicsContext = this.graphicsContext2D
            val background = Stream.of(drawBackground(graphicsContext, environment))
            val effects = effects
                .stream()
                .map<Queue<DrawCommand>> { group -> group.computeDrawCommands(environment) }
                .flatMap<DrawCommand>{ it.stream() }
                .map { cmd -> {cmd.accept(graphicsContext, wormhole) } }
            commandQueue = Stream
                .concat(background, effects)
                .collect(Collectors.toCollection { ConcurrentLinkedQueue<() -> Unit>() })
            repaint()
        } else {
            throw IllegalStateException("Only the simulation thread can dictate GUI updates")
        }
    }

    private fun makePoint(x: Number, y: Number) = Point(x.toInt(), y.toInt())

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
        private const val GET_X_METHOD_NAME = "getX"
        private const val GET_Y_METHOD_NAME = "getY"
    }
}
