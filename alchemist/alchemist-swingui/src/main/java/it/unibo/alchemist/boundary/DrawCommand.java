package it.unibo.alchemist.boundary;

import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Functional interface that models a command for JavaFX thread to draw something on a {@link Canvas}.
 */
@FunctionalInterface
public interface DrawCommand extends BiConsumer<GraphicsContext, BidimensionalWormhole> {

    /**
     * The method consumes a graphic and a wormhole to draw something
     *
     * @param graphic  the {@link GraphicsContext} of a JavaFX {@link Canvas}
     * @param wormhole the {@link BidimensionalWormhole Wormhole} that maps {@link Environment} {@link Position positions} to GUI positions
     */
    @Override
    void accept(GraphicsContext graphic, BidimensionalWormhole wormhole);

    /**
     * Wrapper method that wraps this {@link DrawCommand} into another that checks if should execute or not the {@link #accept(GraphicsContext, BidimensionalWormhole)} method.
     *
     * @param booleanSupplier a condition checker {@link Boolean} {@link Supplier}
     * @return a new {@link DrawCommand} that wraps this one around the if checking
     */
    default DrawCommand wrap(final Supplier<Boolean> booleanSupplier) {
        return (g, wh) -> {
            if (booleanSupplier.get()) {
                this.accept(g, wh);
            }
        };
    }

}
