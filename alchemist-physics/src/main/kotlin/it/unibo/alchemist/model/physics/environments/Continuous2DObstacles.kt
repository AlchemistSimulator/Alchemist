/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.environments

import com.github.davidmoten.rtree.Entry
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry.Geometries
import com.github.davidmoten.rtree.geometry.Rectangle
import com.github.davidmoten.rtree.internal.EntryDefault
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.obstacles.RectObstacle2D
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serial

/**
 * @param T concentration type
 */
open class Continuous2DObstacles<T>(incarnation: Incarnation<T, Euclidean2DPosition>) :
    AbstractLimitedContinuous2D<T>(incarnation),
    EuclideanPhysics2DEnvironmentWithObstacles<RectObstacle2D<Euclidean2DPosition>, T> {

    private companion object {
        private const val TOLERANCE_MULTIPLIER = 0.01

        @Serial
        private const val serialVersionUID = 69931743897405107L
    }

    private var rtree: RTree<RectObstacle2D<Euclidean2DPosition>, Rectangle> = RTree.create()

    override fun addObstacle(o: RectObstacle2D<Euclidean2DPosition>) {
        rtree = rtree.add(o, toGeometry(o))
        includeObject(o.minX, o.maxX, o.minY, o.maxY)
    }

    override val obstacles: List<RectObstacle2D<Euclidean2DPosition>> get() =
        rtree.entries().map(Entry<RectObstacle2D<Euclidean2DPosition>, Rectangle>::value).toList().toBlocking().single()

    override fun getObstaclesInRange(
        center: Euclidean2DPosition,
        range: Double,
    ): List<RectObstacle2D<Euclidean2DPosition>> = getObstaclesInRange(center.x, center.y, range)

    override fun getObstaclesInRange(
        centerx: Double,
        centery: Double,
        range: Double,
    ): List<RectObstacle2D<Euclidean2DPosition>> = rtree.search(
        Geometries.circle(centerx, centery, range),
    ).map(Entry<RectObstacle2D<Euclidean2DPosition>, Rectangle>::value).toList().toBlocking().single()

    override fun hasMobileObstacles(): Boolean = false

    override fun intersectsObstacle(start: Euclidean2DPosition, end: Euclidean2DPosition): Boolean {
        val (sx, sy) = start
        val (ex, ey) = end
        return query(sx, sy, ex, ey, 0.0).any { obstacle ->
            val coords = obstacle.nearestIntersection(start, end).coordinates
            coords[0] != ex || coords[1] != ey || obstacle.contains(coords[0], coords[1])
        }
    }

    override fun isAllowed(p: Euclidean2DPosition): Boolean =
        rtree.search(Geometries.point(p.x, p.y)).isEmpty.toBlocking().single()

    override fun next(current: Euclidean2DPosition, desired: Euclidean2DPosition): Euclidean2DPosition =
        next(current.x, current.y, desired.x, desired.y)

    override fun next(curX: Double, curY: Double, newX: Double, newY: Double): Euclidean2DPosition {
        val obstacles = query(curX, curY, newX, newY, TOLERANCE_MULTIPLIER).toMutableList()
        if (obstacles.isEmpty()) return Euclidean2DPosition(newX, newY)

        var (fx, fy) = newX to newY
        var fxCache: Double
        var fyCache: Double
        do {
            fxCache = fx
            fyCache = fy
            obstacles.iterator().run {
                while (hasNext()) {
                    val shortest = next().next(Euclidean2DPosition(curX, curY), Euclidean2DPosition(fx, fy))
                    val (sfx, sfy) = shortest
                    if (sfx != fx || sfy != fy) {
                        fx = sfx
                        fy = sfy
                        remove()
                    }
                }
            }
        } while (fx != fxCache || fy != fyCache)

        return Euclidean2DPosition(fx, fy)
    }

    private fun query(
        ox: Double,
        oy: Double,
        nx: Double,
        ny: Double,
        tolerance: Double,
    ): List<RectObstacle2D<Euclidean2DPosition>> {
        var (minx, miny) = minOf(ox, nx) to minOf(oy, ny)
        var (maxx, maxy) = maxOf(ox, nx) to maxOf(oy, ny)
        val dx = (maxx - minx) * tolerance
        val dy = (maxy - miny) * tolerance
        minx -= dx
        maxx += dx
        miny -= dy
        maxy += dy
        return rtree.search(Geometries.rectangle(minx, miny, maxx, maxy))
            .map(Entry<RectObstacle2D<Euclidean2DPosition>, Rectangle>::value)
            .toList()
            .toBlocking()
            .single()
    }

    override fun removeObstacle(o: RectObstacle2D<Euclidean2DPosition>): Boolean {
        val initialSize = rtree.size()
        rtree = rtree.delete(o, toGeometry(o))
        return rtree.size() == initialSize - 1
    }

    override fun moveNodeToPosition(node: Node<T>, newPosition: Euclidean2DPosition) =
        super<AbstractLimitedContinuous2D>.moveNodeToPosition(node, newPosition)

    @Serial
    private fun writeObject(o: ObjectOutputStream) {
        o.defaultWriteObject()
        o.writeObject(obstacles)
    }

    @Serial
    private fun readObject(o: ObjectInputStream) {
        o.defaultReadObject()
        rtree = RTree.create()
        @Suppress("UNCHECKED_CAST")
        val obstacles = o.readObject() as List<RectObstacle2D<Euclidean2DPosition>>
        rtree = RTree.create<RectObstacle2D<Euclidean2DPosition>, Rectangle>().add(
            obstacles.parallelStream()
                .map { EntryDefault(it, toGeometry(it)) }
                .toList(),
        )
    }

    private fun toGeometry(o: RectObstacle2D<Euclidean2DPosition>): Rectangle =
        Geometries.rectangle(o.minX, o.minY, o.maxX, o.maxY)
}
