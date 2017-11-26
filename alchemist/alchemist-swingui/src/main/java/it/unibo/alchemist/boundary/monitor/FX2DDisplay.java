package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor;
import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.monitor.generic.AbstractFXDisplay;
import it.unibo.alchemist.boundary.wormhole.implementation.ExponentialZoomManager;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import java.awt.Point;

/**
 * Simple implementation of a monitor that graphically represents a 2D space and simulation.
 *
 * @param <T> The type which describes the {@link Concentration} of a molecule
 */
public class FX2DDisplay<T> extends AbstractFXDisplay<T> implements FX2DOutputMonitor<T> {
    /**
     * Empiric zoom scale value.
     */
    private static final double ZOOM_SCALE = 40.0;
    private ZoomManager zoomManager;

    /**
     * Default constructor. The number of steps is set to default ({@value #DEFAULT_NUMBER_OF_STEPS}).
     */
    public FX2DDisplay() {
        super();
    }

    /**
     * Main constructor. It lets the developer specify the number of steps.
     *
     * @param step the number of steps
     * @see #setStep(int)
     */
    public FX2DDisplay(final int step) {
        super(step);
    }

    /**
     * Lets child-classes access the zoom manager.
     *
     * @return an {@link ZoomManager}
     */
    protected final ZoomManager getZoomManager() {
        return zoomManager;
    }

    /**
     * Lets child-classes change the zoom manager.
     *
     * @param zoomManager an {@link ZoomManager}
     */
    protected void setZoomManager(final ZoomManager zoomManager) {
        this.zoomManager = zoomManager;
        getWormhole().setZoom(zoomManager.getZoom());
    }

    @Override
    public void zoomTo(final Position center, final double zoomLevel) {
        assert center.getDimensions() == 2;
        final BidimensionalWormhole wh = getWormhole();
        if (wh != null) {
            wh.zoomOnPoint(wh.getViewPoint(center), zoomLevel);
        }
    }

    @Override
    protected void initMouseListener() {
        super.initMouseListener();
        setOnScroll(event -> {
            final BidimensionalWormhole wh = getWormhole();
            if (wh != null && zoomManager != null) {
                zoomManager.inc(event.getDeltaY() / ZOOM_SCALE);
                final int mouseX = (int) event.getX();
                final int mouseY = (int) event.getY();
                wh.zoomOnPoint(new Point(mouseX, mouseY), zoomManager.getZoom());
            }
            repaint();
            event.consume();
        });
    }

    @Override
    protected void init(final Environment<T> environment) {
        super.init(environment);
        if (getWormhole() != null) {
            zoomManager = new ExponentialZoomManager(getWormhole().getZoom(), ExponentialZoomManager.DEF_BASE);
        }
    }
}
