/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.environments

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.obstacles.RectObstacle2D
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import org.kaikikm.threadresloader.ResourceLoader

/**
 * This environment loads an image from the classpath or the file system,
 * and marks as obstacles all the pixels of a given color.
 *
 * @param T concentration type
 * @param incarnation the incarnation to be used.
 * @param path the path where to load the image from: a classpath resource or a local file path.
 * @param zoom zoom level.
 * @param dx delta X position.
 * @param dy delta Y position.
 * @param obstacleColor integer representing the RGB color of the obstacles in the image,
 *                      encoded as in [Color.getRGB].
 * @throws IOException if the image cannot be read.
 */
open class ImageEnvironment<T> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    path: String,
    zoom: Double = DEFAULT_ZOOM,
    dx: Double = DEFAULT_DELTA_X,
    dy: Double = DEFAULT_DELTA_Y,
    obstacleColor: Int = DEFAULT_COLOR,
) : Continuous2DObstacles<T>(incarnation) {
    init {
        val image = requireNotNull(
            ResourceLoader.getResourceAsStream(path) ?: File(path).takeIf(File::exists)?.inputStream(),
        ) {
            "Nor resource $path nor file ${File(path).absolutePath} exist"
        }.use { requireNotNull(ImageIO.read(it)) { "$path is not an image in a supported format" } }
        image.markedRegions(obstacleColor).forEach { addObstacle(it.scaled(zoom, dx, dy, image.height)) }
    }

    /**
     * Finds all the regions of [image] marked with [color], the RGB representation of the marker color,
     * as rectangles in image coordinates.
     */
    protected fun findMarkedRegions(color: Int, image: BufferedImage): List<RectObstacle2D<Euclidean2DPosition>> =
        image.markedRegions(color)

    /**
     * Default values for the [ImageEnvironment] parameters.
     */
    companion object {
        /**
         * Default color to be parsed as obstacle.
         */
        @JvmField
        val DEFAULT_COLOR: Int = Color.BLACK.rgb

        /**
         * Default X starting position.
         */
        const val DEFAULT_DELTA_X = 0.0

        /**
         * Default Y starting position.
         */
        const val DEFAULT_DELTA_Y = 0.0

        /**
         * Default zoom level.
         */
        const val DEFAULT_ZOOM = 1.0

        /**
         * The first pixel marked with [color] and not yet [marked], scanning rows from ([fromX], [fromY]).
         */
        private fun BufferedImage.firstMarked(
            color: Int,
            marked: Array<BooleanArray>,
            fromX: Int,
            fromY: Int,
        ): Pair<Int, Int>? {
            var x = fromX
            var y = fromY
            while (y < height) {
                if (x < width) {
                    if (isFree(color, marked, x, y)) {
                        return x to y
                    }
                    x++
                } else {
                    // From now on the search must start from zero
                    x = 0
                    y++
                }
            }
            return null
        }

        private fun BufferedImage.isFree(color: Int, marked: Array<BooleanArray>, x: Int, y: Int): Boolean =
            !marked[x][y] && getRGB(x, y) == color

        private fun BufferedImage.markedRegions(color: Int): List<RectObstacle2D<Euclidean2DPosition>> {
            val marked = Array(width) { BooleanArray(height) }
            val regions = mutableListOf<RectObstacle2D<Euclidean2DPosition>>()
            var start = firstMarked(color, marked, 0, 0)
            while (start != null) {
                val (startX, startY) = start
                val (endX, endY) = regionEnd(color, marked, startX, startY)
                for (x in startX until endX) {
                    marked[x].fill(true, startY, endY)
                }
                regions += RectObstacle2D(
                    startX.toDouble(),
                    startY.toDouble(),
                    (endX - startX).toDouble(),
                    (endY - startY).toDouble(),
                )
                start = when {
                    endX < width -> firstMarked(color, marked, endX, startY)
                    // The region is as large as the entire image
                    startY == 0 -> firstMarked(color, marked, 0, endY)
                    else -> firstMarked(color, marked, 0, startY + 1)
                }
            }
            return regions
        }

        /**
         * The exclusive end of the widest, then tallest, free rectangle of [color] starting at ([startX], [startY]).
         */
        private fun BufferedImage.regionEnd(
            color: Int,
            marked: Array<BooleanArray>,
            startX: Int,
            startY: Int,
        ): Pair<Int, Int> {
            var endX = startX
            while (endX < width && isFree(color, marked, endX, startY)) {
                endX++
            }
            var endY = startY
            while (endY < height && (startX until endX).all { isFree(color, marked, it, endY) }) {
                endY++
            }
            return endX to endY
        }

        /**
         * Maps this obstacle from the coordinates of an image of height [imageHeight] to the environment ones,
         * flipping the y axis.
         */
        private fun RectObstacle2D<Euclidean2DPosition>.scaled(
            zoom: Double,
            dx: Double,
            dy: Double,
            imageHeight: Int,
        ): RectObstacle2D<Euclidean2DPosition> = RectObstacle2D(
            minX * zoom + dx,
            (imageHeight - minY) * zoom + dy,
            width * zoom,
            -height * zoom,
        )
    }
}
