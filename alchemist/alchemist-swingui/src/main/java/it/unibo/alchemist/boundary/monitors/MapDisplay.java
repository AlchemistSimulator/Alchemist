/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

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

import it.unibo.alchemist.boundary.wormhole.implementation.LinearZoomManager;
import it.unibo.alchemist.boundary.wormhole.implementation.MapWormhole;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * 
 * @param <T>
 */
public class MapDisplay<T> extends Generic2DDisplay<T> {
    private static final long serialVersionUID = 8593507198560560646L;
    private static final GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;
    private static final int IN_MEMORY_TILES = 256;
    private static final int ON_DISK_TILES = 2048;
    private static final AtomicInteger IDGEN = new AtomicInteger();
    private final MapView mapView = new MapView();

    /**
     * 
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

    @Override
    protected void drawBackground(final Graphics2D g) {
    }

    @Override
    public void paint(final Graphics g) {
        super.paint(g);
        if (mapView != null) {
            mapView.paint(g);
        }
        drawEnvOnView((Graphics2D) g);
    };

    @Override
    public void initialized(final Environment<T> env) {
        super.initialized(env);
        Arrays.stream(getMouseListeners()).forEach(mapView::addMouseListener);
        Arrays.stream(getMouseMotionListeners()).forEach(mapView::addMouseMotionListener);
        setWormhole(new MapWormhole(env, this, mapView.getModel().mapViewPosition));
        setZoomManager(new LinearZoomManager(1, 1, 2, MapWormhole.MAX_ZOOM));
        getWormhole().center();
        getWormhole().optimalZoom();
        getZoomManager().setZoom(getWormhole().getZoom());
        super.initialized(env);
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
    protected void setDist(final int x, final int y) {
        try {
            super.setDist(x, y);
        } catch (final IllegalArgumentException e) {
            return;
        }
    }

    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {
        /*
         * Shut down the download threads, preventing memory leaks
         */
        mapView.getLayerManager().interrupt();
        super.finished(env, time, step);
    }

}
