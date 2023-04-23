/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.environments;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.obstacles.RectObstacle2D;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Incarnation;
import org.kaikikm.threadresloader.ResourceLoader;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This environment loads an image from the file system, and marks as obstacles all the pixels of a given color.
 * 
 * @param <T> concentration type
 */
public class ImageEnvironment<T> extends Continuous2DObstacles<T> {

    private static final long serialVersionUID = 7820304584505654721L;
    /**
     * Default color to be parsed as obstacle.
     */
    public static final int DEFAULT_COLOR = Color.BLACK.getRGB();
    /**
     * Default zoom level.
     */
    public static final double DEFAULT_ZOOM = 1d;
    /**
     * Default X starting position.
     */
    public static final double DEFAULT_DELTA_X = 0d;
    /**
     * Default Y starting position.
     */
    public static final double DEFAULT_DELTA_Y = 0d;

    /**
     * @param incarnation the incarnation to be used.
     * @param path
     *            the path where to load the image. Must be a local file path.
     * @throws IOException
     *             if image file cannot be found, or if you disconnected your
     *             hard drive while this method was running.
     */
    public ImageEnvironment(final Incarnation<T, Euclidean2DPosition> incarnation, final String path) throws IOException {
        this(incarnation, path, DEFAULT_ZOOM);
    }

    /**
     * @param incarnation the incarnation to be used.
     * @param path
     *            the path where to load the image. Must be a local file path.
     * @param zoom
     *            zoom level
     * @throws IOException
     *             if image file cannot be found, or if you disconnected your
     *             hard drive while this method was running.
     */
    public ImageEnvironment(
        final Incarnation<T, Euclidean2DPosition> incarnation,
        final String path,
        final double zoom
    ) throws IOException {
        this(incarnation, path, zoom, DEFAULT_DELTA_X, DEFAULT_DELTA_Y);
    }

    /**
     * @param incarnation the incarnation to be used.
     * @param path
     *            the path where to load the image. Must be a local file path.
     * @param zoom
     *            zoom level
     * @param dx
     *            delta X position
     * @param dy
     *            delta Y position
     * @throws IOException
     *             if image file cannot be found, or if you disconnected your
     *             hard drive while this method was running.
     */
    public ImageEnvironment(
        final Incarnation<T, Euclidean2DPosition> incarnation,
        final String path,
        final double zoom,
        final double dx,
        final double dy
    ) throws IOException {
        this(incarnation, DEFAULT_COLOR, path, zoom, dx, dy);
    }

    /**
     * @param incarnation the incarnation to be used.
     * @param obstacleColor
     *            integer representing the RGB color to use as color for the
     *            obstacle detection in image. Encoding follows common Java
     *            rules: {@link Color#getRGB()}
     * @param path
     *            the path where to load the image. Must be a local file path.
     * @param zoom
     *            zoom level
     * @param dx
     *            delta X position
     * @param dy
     *            delta Y position
     * @throws IOException
     *             if image file cannot be found, or if you disconnected your
     *             hard drive while this method was running.
     */
    public ImageEnvironment(
            final Incarnation<T, Euclidean2DPosition> incarnation,
            final int obstacleColor,
            final String path,
            final double zoom,
            final double dx,
            final double dy
    ) throws IOException {
        super(incarnation);
        InputStream resource = ResourceLoader.getResourceAsStream(path);
        if (resource == null) {
            final var file = new File(path);
            if (file.exists()) {
                resource = new FileInputStream(file);
            } else {
                throw new IllegalArgumentException("Nor resource " + path + " nor file " + file.getAbsolutePath() + " exist");
            }
        }
        final BufferedImage img = ImageIO.read(resource);
        findMarkedRegions(obstacleColor, img).forEach(obstacle ->
                addObstacle(mapToEnv(obstacle, zoom, dx, dy, img.getHeight()))
        );
    }

    /**
     * Finds all the regions marked with a given color.
     *
     * @param color
     *              the RGB representation of the marker color.
     * @param img
     *              the image.
     * @return
     *              A list of {@link RectObstacle2D} representing the marked regions.
     */
    protected List<RectObstacle2D<Euclidean2DPosition>> findMarkedRegions(final int color, final BufferedImage img) {
        final int w = img.getWidth();
        final int h = img.getHeight();
        final boolean[][] bmat = new boolean[w][h];
        final List<RectObstacle2D<Euclidean2DPosition>> regions = new ArrayList<>();
        int[] sp = searchNext(color, img, new int[2], bmat);
        while (sp != null) {
            final int[] ep = searchObstacleEnd(color, img, sp, bmat);
            setMatrix(bmat, sp, ep);
            regions.add(new RectObstacle2D<>(sp[0], sp[1], ep[0] - sp[0], ep[1] - sp[1]));
            final int[] nsp = new int[2];
            if (ep[0] == w) {
                if (sp[1] == 0) {
                    /*
                     * The region is as large as the entire screen
                     */
                    nsp[1] = ep[1];
                } else {
                    nsp[1] = sp[1] + 1;
                }
            } else {
                nsp[0] = ep[0];
                nsp[1] = sp[1];
            }
            sp = searchNext(color, img, nsp, bmat);
        }
        return regions;
    }

    @Nullable
    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private static int[] searchNext(
        final int color,
        final BufferedImage img,
        final int[] s,
        final boolean[][] bmat
    ) {
        int initx = s[0];
        for (int y = s[1]; y < img.getHeight(); y++) {
            for (int x = initx; x < img.getWidth(); x++) {
                if (!bmat[x][y] && img.getRGB(x, y) == color) {
                    return new int[] { x, y };
                }
            }
            /*
             * From now on the search must start from zero!
             */
            initx = 0;
        }
        return null; // NOPMD: we do want to return null
    }

    private static void setMatrix(final boolean[][] mat, final int[] sp, final int[] ep) {
        for (int i = sp[0]; i < ep[0]; i++) {
            for (int j = sp[1]; j < ep[1]; j++) {
                mat[i][j] = true;
            }
        }
    }

    private static int[] searchObstacleEnd(final int color, final BufferedImage img, final int[] s, final boolean[][] bmat) {
        int x = s[0];
        int y = s[1];
        /*
         * Compute x size
         */
        while (x < img.getWidth() && !bmat[x][y] && img.getRGB(x, y) == color) {
            x++;
        }
        /*
         * Compute max y size
         */
        while (y < img.getHeight() && lineIsIncluded(y, s[0], x, color, img, bmat)) {
            y++;
        }
        return new int[] { x, y };
    }

    private static boolean lineIsIncluded(
            final int y,
            final int xs,
            final int x,
            final int color,
            final BufferedImage img,
            final boolean[][] bmat
    ) {
        int i = xs;
        while (i < x && !bmat[i][y] && img.getRGB(i, y) == color) {
            i++;
        }
        return i == x;
    }

    private static RectObstacle2D<Euclidean2DPosition> mapToEnv(
            final RectObstacle2D<Euclidean2DPosition> obs, final double zoom, final double dx, final double dy, final int h) {
        return new RectObstacle2D<>(
                obs.x * zoom + dx,
                (h - obs.y) * zoom + dy,
                obs.width * zoom,
                -obs.height * zoom);
    }

}
