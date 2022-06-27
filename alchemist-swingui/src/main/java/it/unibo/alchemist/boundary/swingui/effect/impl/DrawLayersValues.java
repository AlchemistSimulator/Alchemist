/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.impl;

import it.unibo.alchemist.boundary.swingui.effect.api.FunctionDrawer;
import it.unibo.alchemist.boundary.swingui.effect.api.LayerToFunctionMapper;
import it.unibo.alchemist.boundary.ui.api.Wormhole2D;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.danilopianini.view.ExportForGUI;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.function.Function;

/**
 * This is a basic class for all the effects meant to draw {@link it.unibo.alchemist.model.interfaces.Layer}s
 * values in different points of the view. One effect could draw isolines,
 * whereas another could represent different values with a gradient.
 *
 * Normally, drawing a layer's values only makes sense for "numerical" layers
 * (i.e. layers for which the values are {@link Number}s). However, one could have
 * a "non-numerical" layer whose
 * {@link it.unibo.alchemist.model.interfaces.Layer#getValue(it.unibo.alchemist.model.interfaces.Position)}
 * return type is an object from which a value can be extracted somehow. In the end,
 * drawing a layer's values makes sense as long as there is a way to map
 * those values to Numbers. More generally, a {@link LayerToFunctionMapper} is needed.
 * As this class is not aware of which mapper to use, this responsibility is left to subclasses.
 *
 * When drawing layers values, it can be important to know the min and max
 * layer values that will be drawn. This class declares gui controls
 * that allow the user to specify such boundaries.
 */
public abstract class DrawLayersValues extends AbstractDrawLayers implements FunctionDrawer {

    private static final long serialVersionUID = 1L;
    @ExportForGUI(nameToExport = "Min layer value")
    private String minLayerValue = "0.0";
    @ExportForGUI(nameToExport = "Max layer value")
    private String maxLayerValue = "0.0";
    private String minLayerValueCached = minLayerValue;
    private String maxLayerValueCached = maxLayerValue;
    private Double minLayerValueDouble = Double.parseDouble(minLayerValueCached);
    private Double maxLayerValueDouble = Double.parseDouble(maxLayerValueCached);
    private final LayerToFunctionMapper mapper;

    DrawLayersValues(final LayerToFunctionMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, P extends Position2D<P>> void drawLayers(
            final Collection<Layer<T, P>> toDraw,
            final Environment<T, P> environment,
            final Graphics2D graphics,
            final Wormhole2D<P> wormhole
    ) {
        mapper.prepare(this, toDraw, environment, graphics, wormhole);
        mapper.map(toDraw.stream()).forEach(f -> this.drawFunction(f, environment, graphics, wormhole));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract <T, P extends Position2D<P>> void drawFunction(
            Function<? super P, ? extends Number> function,
            Environment<T, P> environment,
            Graphics2D graphics,
            Wormhole2D<P> wormhole
    );

    /**
     * @return a boolean representing whether or not min and max layer values should be updated
     */
    protected boolean minOrMaxLayerValuesNeedsToBeUpdated() {
        return !minLayerValueCached.equals(minLayerValue) || !maxLayerValueCached.equals(maxLayerValue);
    }

    /**
     * Update min and max layer values.
     */
    protected void updateMinAndMaxLayerValues() {
        minLayerValueCached = minLayerValue;
        maxLayerValueCached = maxLayerValue;
        try {
            minLayerValueDouble = Double.parseDouble(minLayerValueCached);
            maxLayerValueDouble = Double.parseDouble(maxLayerValueCached);
        } catch (NumberFormatException e) {
            L.warn(minLayerValue + " or " + maxLayerValue + " are not valid values");
        }
    }

    /**
     * @return a string representation of the min layer value
     */
    public String getMinLayerValue() {
        return minLayerValue;
    }

    /**
     * @param minLayerValue a string representation of the min layer value to set
     */
    public void setMinLayerValue(final String minLayerValue) {
        this.minLayerValue = minLayerValue;
    }

    /**
     * @return a string representation of the max layer value
     */
    public String getMaxLayerValue() {
        return maxLayerValue;
    }

    /**
     * @param maxLayerValue a string representation of the max layer value to set
     */
    public void setMaxLayerValue(final String maxLayerValue) {
        this.maxLayerValue = maxLayerValue;
    }

    /**
     * @return a string representation of the min layer value cached
     */
    protected String getMinLayerValueCached() {
        return minLayerValueCached;
    }

    /**
     * @param minLayerValueCached a string representation of the min layer value to cache
     */
    protected void setMinLayerValueCached(final String minLayerValueCached) {
        this.minLayerValueCached = minLayerValueCached;
    }

    /**
     * @return a string representation of the max layer value cached
     */
    protected String getMaxLayerValueCached() {
        return maxLayerValueCached;
    }

    /**
     * @param maxLayerValueCached a string representation of the max layer value to cache
     */
    protected void setMaxLayerValueCached(final String maxLayerValueCached) {
        this.maxLayerValueCached = maxLayerValueCached;
    }

    /**
     * @return the min layer value
     */
    protected Double getMinLayerValueDouble() {
        return minLayerValueDouble;
    }

    /**
     * @param minLayerValueDouble the min layer value to set
     */
    protected void setMinLayerValueDouble(final Double minLayerValueDouble) {
        this.minLayerValueDouble = minLayerValueDouble;
    }

    /**
     * @return the max layer value
     */
    protected Double getMaxLayerValueDouble() {
        return maxLayerValueDouble;
    }

    /**
     * @param maxLayerValueDouble the max layer value to set
     */
    protected void setMaxLayerValueDouble(final Double maxLayerValueDouble) {
        this.maxLayerValueDouble = maxLayerValueDouble;
    }
}
