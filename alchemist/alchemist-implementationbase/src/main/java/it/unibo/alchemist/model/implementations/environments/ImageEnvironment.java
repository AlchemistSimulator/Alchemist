/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import it.unibo.alchemist.model.implementations.utils.RectObstacle2D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.kaikikm.threadresloader.ResourceLoader;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This environment loads an image from the file system, and marks as obstacles all the pixels of a given color.
 * 
 * @param <T>
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
     * @param path
     *            the path where to load the image. Must be a local file path.
     * @throws IOException
     *             if image file cannot be found, or if you disconnected your
     *             hard drive while this method was running.
     */
    public ImageEnvironment(final String path) throws IOException {
        this(path, DEFAULT_ZOOM);
    }

    /**
     * @param path
     *            the path where to load the image. Must be a local file path.
     * @param zoom
     *            zoom level
     * @throws IOException
     *             if image file cannot be found, or if you disconnected your
     *             hard drive while this method was running.
     */
    public ImageEnvironment(final String path, final double zoom) throws IOException {
        this(path, zoom, DEFAULT_DELTA_X, DEFAULT_DELTA_Y);
    }

    /**
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
    public ImageEnvironment(final String path, final double zoom, final double dx, final double dy) throws IOException {
        this(DEFAULT_COLOR, path, zoom, dx, dy);
    }

    /**
     * @param obs
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
    public ImageEnvironment(final int obs, final String path, final double zoom, final double dx, final double dy) throws IOException {
        super();
        final InputStream resource = ResourceLoader.getResourceAsStream(path);
        final BufferedImage img = resource == null 
                ? ImageIO.read(new File(path))
                : ImageIO.read(resource);
        final int w = img.getWidth();
        final int h = img.getHeight();
        final boolean[][] bmat = new boolean[w][h];
        int[] sp = searchNext(obs, img, new int[2], bmat);
        while (sp != null) {
            final int[] ep = searchObstacleEnd(obs, img, sp, bmat);
            setMatrix(bmat, sp, ep);
            final double startx = sp[0] * zoom + dx;
            final double starty = (h - sp[1]) * zoom + dy;
            final double width = (ep[0] - sp[0]) * zoom;
            final double height = -(ep[1] - sp[1]) * zoom;
            addObstacle(new RectObstacle2D(startx, starty, width, height));
            final int[] nsp = new int[2];
            if (ep[0] == w) {
                if (sp[1] == 0) {
                    /*
                     * The obstacle is as large as the entire screen
                     */
                    nsp[1] = ep[1];
                } else {
                    nsp[1] = sp[1] + 1;
                }
            } else {
                nsp[0] = ep[0];
                nsp[1] = sp[1];
            }
            sp = searchNext(obs, img, nsp, bmat);
        }
    }

    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private static int[] searchNext(final int color, final BufferedImage img, final int[] s, final boolean[][] bmat) {
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
        return null;
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

    private static boolean lineIsIncluded(final int y, final int xs, final int x, final int color, final BufferedImage img, final boolean[][] bmat) {
        int i = xs;
        while (i < x && !bmat[i][y] && img.getRGB(i, y) == color) {
            i++;
        }
        return i == x;
    }

}
