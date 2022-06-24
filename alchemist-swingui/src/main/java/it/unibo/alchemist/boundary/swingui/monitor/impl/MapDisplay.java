/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.monitor.impl;

import it.unibo.alchemist.boundary.ui.impl.LinearZoomManager;
import it.unibo.alchemist.boundary.util.InitMapsForge;
import it.unibo.alchemist.boundary.wormhole.impl.MapWormhole;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.Time;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.TwoLevelTileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.download.tilesource.TileSource;
import org.mapsforge.map.model.Model;

import javax.annotation.Nonnull;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Graphical 2D display of an environments that uses a map.
 *
 * @param <T> the {@link it.unibo.alchemist.model.interfaces.Concentration} type
 */
@Deprecated
public final class MapDisplay<T> extends Generic2DDisplay<T, GeoPosition> {
    private static final long serialVersionUID = 8593507198560560646L;
    private static final GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;
    private static final int IN_MEMORY_TILES = 256;
    private static final int ON_DISK_TILES = 2048;
    private static final AtomicInteger IDGEN = new AtomicInteger();
    private final MapView mapView = new MapView();

    static {
        InitMapsForge.initAgent();
    }
    /**
     * Default constructor.
     */
    public MapDisplay() {
        super();
        setLayout(new BorderLayout());
        final TileDownloadLayer tdl = createTileDownloadLayer(createTileCache(), mapView.getModel());
        mapView.addLayer(tdl);
        tdl.start();
        mapView.getMapScaleBar().setVisible(true);
        add(mapView);
    }

    private static TileCache createTileCache() {
        final TileCache firstLevelTileCache = new InMemoryTileCache(IN_MEMORY_TILES);
        final String tmpdir = System.getProperty("java.io.tmpdir");
        final File cacheDirectory = new File(tmpdir, "mapsforge" + IDGEN.getAndIncrement());
        final TileCache secondLevelTileCache = new FileSystemTileCache(ON_DISK_TILES, cacheDirectory, GRAPHIC_FACTORY);
        return new TwoLevelTileCache(firstLevelTileCache, secondLevelTileCache);
    }

    private static TileDownloadLayer createTileDownloadLayer(final TileCache tileCache, final Model model) {
        final TileSource tileSource = OpenStreetMapMapnik.INSTANCE;
        final TileDownloadLayer tdl = new TileDownloadLayer(
                tileCache,
                model.mapViewPosition,
                tileSource,
                GRAPHIC_FACTORY);
        tdl.setDisplayModel(model.displayModel);
        return tdl;
    }

    @Override
    protected void drawBackground(final Graphics2D g) {
    }

    @Override
    public void paint(final Graphics g) {
        if (g instanceof Graphics2D) {
            super.paint(g);
            if (mapView != null) {
                mapView.paint(g);
            }
            drawEnvOnView((Graphics2D) g);
        } else {
            throw new IllegalArgumentException("Graphics2D is required");
        }
    }

    @Override
    public void initialized(@Nonnull final Environment<T, GeoPosition> environment) {
        super.initialized(environment);
        // Remove the MapView default listeners (otherwise, the map view uses its drag effect)
        Arrays.stream(mapView.getMouseListeners()).forEach(mapView::removeMouseListener);
        Arrays.stream(mapView.getMouseMotionListeners()).forEach(mapView::removeMouseMotionListener);
        // Add the listeners implemented in generic (otherwise, no events are handled when users interact with the map)
        Arrays.stream(getMouseListeners()).forEach(mapView::addMouseListener);
        Arrays.stream(getMouseMotionListeners()).forEach(mapView::addMouseMotionListener);
        setWormhole(new MapWormhole(environment, this, mapView.getModel().mapViewPosition));
        setZoomManager(new LinearZoomManager(1, 1, 2, MapWormhole.MAX_ZOOM));
        getWormhole().center();
        getWormhole().optimalZoom();
        getZoomManager().setZoom(getWormhole().getZoom());
        super.initialized(environment);
    }

    @Override
    protected void setMouseTooltipTo(final int x, final int y) {
        super.setMouseTooltipTo(x, y);
    }

    @Override
    public void finished(@Nonnull final Environment<T, GeoPosition> environment, @Nonnull final Time time, final long step) {
        /*
         * Shut down the download threads, preventing memory leaks
         */
        mapView.getLayerManager().interrupt();
        super.finished(environment, time, step);
    }

}
