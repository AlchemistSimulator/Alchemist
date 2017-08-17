package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

/**
 * Base-class for each display able a graphically represent a 2D space
 * and simulation.
 * <p>
 * Tries to use OpenGL acceleration when possible.
 *
 * @param <T> the {@link Concentration} type
 */
public abstract class AbstractFXDisplay<T> extends Canvas implements FXOutputMonitor<T> {

    /**
     * The default frame rate.
     */
    public static final byte DEFAULT_FRAME_RATE = 25;
    /**
     * The default time per frame.
     */
    public static final /* TODO float */ double TIME_STEP = 1 / DEFAULT_FRAME_RATE;
    /**
     * The default radius of freedom of representation.
     */
    protected static final /* TODO byte */ double FREEDOM_RADIUS = 1;
    /**
     * Default logger for this class.
     */
    private static final Logger L = LoggerFactory.getLogger(AbstractFXDisplay.class);
    // TODO

    static {
        System.setProperty("sun.java2d.opengl", "true");
    }

    private int step;
    private IWormhole2D wormhole;
    private double mouseX;
    private double mouseY;
    private Node<T> nearest;
    private ViewStatus status = ViewStatus.MARK_CLOSER;
    private Environment<T> currentEnv;
    private double lastTime;

    public AbstractFXDisplay() {
        this(1);
    }

    public AbstractFXDisplay(final int step) {
        super();
        setStep(step);
        setStyle("-fx-background-color: #FFF;");
        setMouseListener();
        setkeyboardListener();
    }

    private void setkeyboardListener() {
        // TODO
    }

    protected void setMouseListener() {
        setOnMouseClicked(e -> {
            setDist(e.getX(), e.getY());
            if (isCloserNodeMarked() && nearest != null && e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                final NodeTracker<T> monitor = new NodeTracker<>(nearest);
                monitor.stepDone(currentEnv, null, new DoubleTime(lastTime), step);
                final Simulation<T> simulation = currentEnv.getSimulation();
                // TODO open a drawer to the right of MainApp v
//                makeFrame("Tracker for node " + nearest.getId(), monitor, jf -> {
//                    final JFrame frame = (JFrame) jf;
//                    if (sim != null) {
//                        sim.addOutputMonitor(monitor);
//                        frame.addWindowListener(new WindowAdapter() {
//                            @Override
//                            public void windowClosing(final WindowEvent e) {
//                                sim.removeOutputMonitor(monitor);
//                            }
//                        });
//                    }
//                });
                // TODO open a drawer to the right of MainApp ^
            }
        });
        // TODO
    }

    private final boolean isCloserNodeMarked() {
        return status == AbstractFXDisplay.ViewStatus.MARK_CLOSER;
    }

    /**
     * Sets a new {@link Tooltip} on this component with nearest node from the mouse position.
     *
     * @param x x coordinate of the mouse
     * @param y y coordinate of the mouse
     */
    private void setDist(final double x, final double y) {
        if (wormhole != null) {
            mouseX = x;
            mouseY = y;
            final Position envMouse = wormhole.getEnvPoint(new Point((int) mouseX, (int) mouseY));
            final StringBuilder sb = new StringBuilder();
            sb.append(envMouse);
            if (nearest != null) {
                sb.append(" -- ");
                sb.append(ResourceLoader.getStringRes("nearest_node_is"));
                sb.append(": ");
                sb.append(nearest.getId());
            }
            final Tooltip tooltip = new Tooltip(sb.toString());
            Tooltip.install(this, tooltip);
        }
    }

    @Override
    public int getStep() {
        // TODO
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the step is not bigger than 0
     */
    @Override
    public void setStep(final int step) throws IllegalArgumentException {
        if (step <= 0) {
            throw new IllegalArgumentException("The parameter must be a positive integer");
        }
        this.step = step;
    }

    @Override
    public boolean isMarkCloserNode() {
        // TODO
        return false;
    }

    @Override
    public void setMarkCloserNode(final boolean mark) {
        // TODO
    }

    @Override
    public boolean isRealTime() {
        // TODO
        return false;
    }

    @Override
    public void setRealTime(final boolean realTime) {
        // TODO
    }

    @Override
    public void repaint() {
        // TODO GraphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight())
    }

    @Override
    public void setEffectStack(final Collection<EffectFX> effects) {
        // TODO
    }

    @Override
    public void finished(final Environment<T> environment, final Time time, final long step) {
        // TODO
    }

    @Override
    public void initialized(final Environment<T> environment) {
        // TODO
    }

    @Override
    public void stepDone(final Environment<T> environment, final Reaction<T> reaction, final Time time, final long step) {
        // TODO
    }

    private enum ViewStatus {
        VIEW_ONLY,

        MARK_CLOSER,

        SELECTING,

        MOVING,

        CLONING,

        DELETING,

        MOLECULING;
    }
}
