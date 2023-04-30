/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.environments

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.euclidean.geometry.AwtMutableConvexPolygon
import it.unibo.alchemist.model.euclidean.geometry.ConvexPolygon
import it.unibo.alchemist.model.euclidean.geometry.Segment2D
import it.unibo.alchemist.model.euclidean.geometry.navigationgraph.DirectedEuclidean2DNavigationGraph
import it.unibo.alchemist.model.euclidean.geometry.navigationgraph.Euclidean2DNavigationGraph
import it.unibo.alchemist.model.euclidean.geometry.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.euclidean.geometry.navigator.NaviGator.generateNavigationGraph
import it.unibo.alchemist.model.euclidean.obstacles.RectObstacle2D
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.model.geometry.Vector2D
import it.unibo.alchemist.model.physics.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.physics.environments.ImageEnvironment
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
    incarnation: Incarnation<T, Euclidean2DPosition>,
    path: String,
    zoom: Double = 1.0,
    dx: Double = 0.0,
    dy: Double = 0.0,
    obstaclesColor: Int = Color.BLACK.rgb,
    roomsColor: Int = Color.BLUE.rgb,
) : ImageEnvironment<T>(incarnation, obstaclesColor, path, zoom, dx, dy),
    EuclideanPhysics2DEnvironmentWithGraph<RectObstacle2D<Euclidean2DPosition>, T, ConvexPolygon, Euclidean2DPassage> {

    override val graph: Euclidean2DNavigationGraph

    init {
        val resource = ResourceLoader.getResourceAsStream(path)
        val img = if (resource == null) {
            ImageIO.read(File(path))
        } else {
            ImageIO.read(resource)
        }
        val obstacles = findMarkedRegions(obstaclesColor, img)
        val rooms = findMarkedRegions(roomsColor, img).map { Euclidean2DPosition(it.x, it.y) }
        graph = generateNavigationGraph(
            width = img.width.toDouble(),
            height = img.height.toDouble(),
            obstacles = obstacles,
            rooms = rooms,
        ).map { Euclidean2DPosition(it.x * zoom + dx, (img.height - it.y) * zoom + dy) }
    }

    private fun Euclidean2DNavigationGraph.map(
        mapper: (Euclidean2DPosition) -> Euclidean2DPosition,
    ): Euclidean2DNavigationGraph {
        val newGraph = DirectedEuclidean2DNavigationGraph(Euclidean2DPassage::class.java)
        vertexSet().forEach { newGraph.addVertex(it.mapPolygon(mapper)) }
        edgeSet().forEach {
            val mappedTail = it.tail.mapPolygon(mapper)
            val mappedHead = it.head.mapPolygon(mapper)
            val mappedShape = it.passageShapeOnTail.mapSegment(mapper)
            newGraph.addEdge(mappedTail, mappedHead, Euclidean2DPassage(mappedTail, mappedHead, mappedShape))
        }
        return newGraph
    }

    private fun <V : Vector2D<V>> Segment2D<V>.mapSegment(mapper: (V) -> V): Segment2D<V> =
        copyWith(mapper.invoke(first), mapper.invoke(second))

    private fun ConvexPolygon.mapPolygon(mapper: (Euclidean2DPosition) -> Euclidean2DPosition) =
        AwtMutableConvexPolygon(vertices().map(mapper).toMutableList())
}
