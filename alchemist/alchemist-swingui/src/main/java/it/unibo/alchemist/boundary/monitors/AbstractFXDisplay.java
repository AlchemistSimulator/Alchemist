package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import javafx.scene.canvas.Canvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    /** Default logger for this class. */
    private static final Logger L = LoggerFactory.getLogger(AbstractFXDisplay.class);
    // TODO

    static {
        System.setProperty("sun.java2d.opengl", "true");
    }

    AbstractFXDisplay() {
        // TODO
    }

    @Override
    public int getStep() {
        // TODO
        return 0;
    }

    @Override
    public void setStep(final int step) {
        // TODO
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
}
