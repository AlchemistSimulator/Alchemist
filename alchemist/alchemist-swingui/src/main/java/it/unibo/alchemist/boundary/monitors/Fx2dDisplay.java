package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.wormhole.implementation.*;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.boundary.wormhole.interfaces.PointerSpeed;
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.*;
import org.apache.commons.math3.util.Pair;
import org.danilopianini.lang.LangUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Fx2dDisplay<T> extends Generic2DDisplay<T> {
    private static final Logger L = LoggerFactory.getLogger(Fx2dDisplay.class);
    private static final long serialVersionUID = 1L;

    static {
        System.setProperty("sun.java2d.opengl", "true");
    }

    private final Semaphore mapConsistencyMutex = new Semaphore(1);
    private final transient PointerSpeed mouseMovement = new PointerSpeedImpl();
    private final ConcurrentMap<Node<T>, Neighborhood<T>> neighbors = new ConcurrentHashMap<>();
    private final ConcurrentMap<Node<T>, Position> positions = new ConcurrentHashMap<>();
    private transient AngleManagerImpl angleManager;
    private Environment<T> currentEnv;
    private Collection<EffectFX> effectStack;
    private volatile boolean firstTime = true;
    private boolean paintLinks;
    private transient Optional<Node<T>> hooked = Optional.empty();
    private boolean initialized;
    private double lastTime;
    private int mouseX, mouseY;
    private Node<T> nearest;
    private List<? extends Obstacle2D> obstacles;
    private boolean realTime;
    private int st;
    private long timeInit = System.currentTimeMillis();
    private transient IWormhole2D wormhole;
    private transient ZoomManager zoomManager;
    private transient boolean isPreviousStateMarking = true;
    private it.unibo.alchemist.boundary.monitors.Generic2DDisplay.ViewStatus status = it.unibo.alchemist.boundary.monitors.Generic2DDisplay.ViewStatus.MARK_CLOSER;
    private transient boolean isDraggingMouse;
    private transient Optional<Point> originPoint = Optional.empty();
    private transient Optional<Point> endingPoint = Optional.empty();
    private transient Set<Node<T>> selectedNodes = new HashSet<>();

    /**
     * Initializes a new display with out redrawing the first step.
     */
    public Fx2dDisplay() {
        this(1);
        // TODO
    }
    /**
     * Initializes a new display.
     *
     * @param step number of steps to let pass without re-drawing
     */
    public Fx2dDisplay(final int step) {
        super(step);
        // TODO
    }

    @Override
    public void finished(final Environment<T> environment, final Time time, final long step) {
        super.finished(environment, time, step);
        // TODO check
    }

    @Override
    public void initialized(final Environment<T> environment) {
        super.initialized(environment);
        // TODO check
    }

    @Override
    public void stepDone(Environment<T> environment, Reaction<T> reaction, Time time, long step) {
        super.stepDone(environment, reaction, time, step);
        // TODO check
    }

    @Override
    protected final void drawEnvOnView(final Graphics2D g) {
        // TODO
    }

    @Override
    public void setEffectStack(final List<Effect> l) {
        // TODO throw exception, shouldn't use it
    }

    public void setEffectFXStack(final List<EffectFX> l) {
        // TODO use this instead
    }

    @Override
    protected void update(final Environment<T> env, final Time time) {
        // TODO
    }
}

