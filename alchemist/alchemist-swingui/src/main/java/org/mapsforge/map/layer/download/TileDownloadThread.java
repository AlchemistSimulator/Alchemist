/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package org.mapsforge.map.layer.download;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.kaikikm.threadresloader.ResourceLoader;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.map.awt.graphics.AwtTileBitmap;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.queue.JobQueue;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.util.PausableThread;

class TileDownloadThread extends PausableThread {
    private final DisplayModel displayModel;
    private final GraphicFactory graphicFactory;
    private JobQueue<DownloadJob> jobQueue;
    private final Layer layer;
    private final TileCache tileCache;

    TileDownloadThread(
            final TileCache tileCache,
            final JobQueue<DownloadJob> jobQueue,
            final Layer layer,
            final GraphicFactory graphicFactory,
            final DisplayModel displayModel) {
        this.tileCache = tileCache;
        this.jobQueue = jobQueue;
        this.layer = layer;
        this.graphicFactory = graphicFactory;
        this.displayModel = displayModel;
    }

    public void setJobQueue(final JobQueue<DownloadJob> jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    protected void doWork() throws InterruptedException {
        final DownloadJob downloadJob = this.jobQueue.get();
        this.layer.requestRedraw();
        if (!this.tileCache.containsKey(downloadJob)) {
            try {
                tileCache.put(downloadJob, downloadTile(downloadJob));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.jobQueue.remove(downloadJob);
    }

    @Override
    protected ThreadPriority getThreadPriority() {
        return ThreadPriority.BELOW_NORMAL;
    }

    @Override
    protected boolean hasWork() {
        return true;
    }

    private TileBitmap downloadTile(final DownloadJob downloadJob) throws IOException {
        final TileDownloader tileDownloader = new TileDownloader(downloadJob, this.graphicFactory);
        TileBitmap bitmap;
        try {
            bitmap = tileDownloader.downloadImage();
        } catch (IOException e) {
            final BufferedImage img = ImageIO.read(ResourceLoader.getResourceAsStream("other/nointernet.png"));
            bitmap = new AwtTileBitmap(img);
        }
        bitmap.scaleTo(this.displayModel.getTileSize(), this.displayModel.getTileSize());
        return bitmap;
    }
}
