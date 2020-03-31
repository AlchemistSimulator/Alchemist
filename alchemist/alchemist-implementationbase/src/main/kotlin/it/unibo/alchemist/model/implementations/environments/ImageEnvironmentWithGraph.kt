/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.MutableConvexPolygonImpl
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.navigator.generateNavigationGraph
import it.unibo.alchemist.model.interfaces.graph.twod.Euclidean2DNavigationGraph
import it.unibo.alchemist.model.interfaces.graph.twod.Euclidean2DNavigationGraphBuilder
import it.unibo.alchemist.model.interfaces.graph.twod.Euclidean2DCrossing
import it.unibo.alchemist.model.implementations.graph.edges
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.RectObstacle2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.graph.GraphEdgeWithData
import org.kaikikm.threadresloader.ResourceLoader
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

/**
 * An [ImageEnvironment] providing an [Euclidean2DNavigationGraph].
 * The NaviGator algorithm is used to produce such graph (see [generateNavigationGraph]).
 * The positions where to plant initial seeds should be specified directly in the image,
 * marking each area of the environment with one or more pixels of a given color (defaults
 * to blue).
 */
class ImageEnvironmentWithGraph<T> @JvmOverloads constructor(
    path: String,
    zoom: Double = 1.0,
    vararg destinationCoords: Double,
    dx: Double = 0.0,
    dy: Double = 0.0,
    obstaclesColor: Int = Color.BLACK.rgb,
    roomsColor: Int = Color.BLUE.rgb
) : ImageEnvironment<T>(obstaclesColor, path, zoom, dx, dy),
    EuclideanPhysics2DEnvironmentWithGraph<RectObstacle2D, T, ConvexPolygon, Euclidean2DCrossing> {

    init {
        require(destinationCoords.size % 2 == 0) { "missing coordinates" }
    }

    private val navigationGraph: Euclidean2DNavigationGraph by lazy {
        val resource = ResourceLoader.getResourceAsStream(path)
        val img = if (resource == null) {
            ImageIO.read(File(path))
        } else {
            ImageIO.read(resource)
        }
        val obstacles = super.findMarkedRegions(obstaclesColor, img)
        val rooms = super.findMarkedRegions(roomsColor, img)
            .map { Euclidean2DPosition(it.x, it.y) }
        val destinations = mutableListOf<Euclidean2DPosition>()
        for (i in 0..destinationCoords.size - 2 step 2) {
            destinations.add(Euclidean2DPosition(destinationCoords[i], destinationCoords[i + 1]))
        }
        generateNavigationGraph(
            width = img.width.toDouble(),
            height = img.height.toDouble(),
            obstacles = obstacles,
            rooms = rooms,
            destinations = destinations
        ).map { Euclidean2DPosition(it.x * zoom + dx, (img.height - it.y) * zoom + dy) }
    }

    override fun graph() = navigationGraph

    private fun Euclidean2DNavigationGraph.map(
        mapper: (Euclidean2DPosition) -> Euclidean2DPosition
    ): Euclidean2DNavigationGraph {
        val builder = Euclidean2DNavigationGraphBuilder()
        nodes().forEach { builder.addNode(it.mapPolygon(mapper)) }
        edges().forEach { builder.addEdge(it.mapCrossing(mapper)) }
        return builder.build(destinations())
    }

    private fun Euclidean2DCrossing.mapCrossing(
        mapper: (Euclidean2DPosition) -> Euclidean2DPosition
    ): Euclidean2DCrossing =
        GraphEdgeWithData(
            tail.mapPolygon(mapper),
            head.mapPolygon(mapper),
            Pair(mapper.invoke(data.first), mapper.invoke(data.second))
        )

    private fun ConvexPolygon.mapPolygon(mapper: (Euclidean2DPosition) -> Euclidean2DPosition) =
        MutableConvexPolygonImpl(vertices().map(mapper).toMutableList())
}
