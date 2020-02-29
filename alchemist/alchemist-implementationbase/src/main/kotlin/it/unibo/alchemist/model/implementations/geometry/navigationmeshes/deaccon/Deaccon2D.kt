package it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon

import it.unibo.alchemist.model.implementations.geometry.intersects
import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.geometry.isXAxisAligned
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.geometry.SegmentsIntersectionTypes
import it.unibo.alchemist.model.implementations.geometry.findPointOnLineGivenX
import it.unibo.alchemist.model.implementations.geometry.findPointOnLineGivenY
import it.unibo.alchemist.model.implementations.geometry.isAxisAligned
import it.unibo.alchemist.model.implementations.graph.Euclidean2DCrossing
import it.unibo.alchemist.model.implementations.graph.builder.NavigationGraphBuilder
import it.unibo.alchemist.model.implementations.graph.builder.addEdge
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.navigationmeshes.deaccon.ExtendableConvexPolygon
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape
import java.awt.geom.Point2D
import kotlin.math.sqrt

/**
 * DEACCON (Decomposition of Environments for the Creation of Convex-region
 * Navigation-meshes) is an algorithm capable of generating a navigation mesh
 * of a given environment with obstacles.
 *
 * A navigation mesh is a collection of two-dimensional convex polygons
 * representing which areas of an environment are traversable by agents
 * (namely, walkable areas). Since convex polygons are generated, pedestrians
 * can freely walk around within these areas, as it is guaranteed that no obstacle
 * will be found.
 *
 * Deaccon works with rectangular shaped bidimensional environments with euclidean
 * geometry and double precision coordinates (i.e. with [Euclidean2DPosition]s).
 *
 * For more information about the deaccon algorithm, see the related paper:
 * https://www.aaai.org/Papers/AIIDE/2008/AIIDE08-029.pdf
 * The algorithm implemented here is a slight simplification of the one described
 * in the linked paper. Please be aware of the following:
 * - this algorithm does not guarantee the coverage of 100% of the walkable area.
 * - this algorithm was implemented without bothering too much with performance,
 * since the generation of navigation meshes is usually a pre-processing phase.
 * Be aware that such operation can take a significant amount of time.
 */
/*
 * Here's a brief description of how the algorithm operates:
 * PHASE 1: SEEDING
 * A certain number of seeds is planted in the environment. Each seed is a
 * square-shaped region that will grow maintaining a convex shape.
 * PHASE 2: GROWING
 * Planted seeds are extended until possible (i.e. until they are in contact
 * with an obstacle or another seed on each side)
 * PHASE 3: CLEAN-UP
 * Adjacent regions are combined if the resulting polygon is still convex.
 *
 * In certain cases, performing only the above mentioned phases may result in
 * a poor coverage of the walkable area. In order to avoid this, another operation
 * called active seeding is performed. Basically, other seeds are generated,
 * but much smaller. After that, the three phases described above are repeated for
 * these newly generated seeds. Such seeds are called "active" because their purpose
 * is to "cover" the holes resulting from the first three phases. In the future,
 * the active seeding phase may be modified with something smarter. For instance,
 * uncovered walkable regions may be located and extra seeds may be planted exactly
 * in these locations, instead of in the whole environment.
 */
class Deaccon2D(
    /**
     * Number of seeds to generate in the first phase of the algorithm. Note
     * that the area that will initially be covered by seeds DO NOT grow with
     * the number of seeds. In fact, the proportion of the environment's area
     * to be covered in the initial seeding phase is fixed. By changing the
     * number of seeds, what changes is their dimension. With their total area
     * fixed, generating a lower number of seeds will result in coarse-grained
     * initial seeds. Whereas generating a lot of seeds will result in fine-grained
     * seeds. Ultimately, altering this parameter affects the grain of the initial
     * seeds. This is highly dependent on the particular environment (if you have a
     * single room coarse-grained seeds are the best, whereas if you have a whole
     * building you'd better go with fine-grained ones or the resulting navigation
     * mesh may be poor). It may be advisable to try different quantity of seeds
     * and pick the best trade-off between coverage of walkable area and time.
     * Generally speaking, the more detailed your environment is, the higher
     * this quantity should be.
     */
    private val nSeeds: Int = 100
) {

    /*
     * These are all the parameters of the algorithm. Their current values
     * are the empirically most suitable ones (namely the ones leading to
     * a better coverage of the walkable area).
     *
     * If you obtain a poor coverage even after altering the number of initial
     * seeds, you should think of altering these parameters.
     */
    companion object {
        /**
         * Represents the % of the environment area to cover with seeds
         * in the initial seeding phase
         */
        const val AREA_TO_COVER = 0.5
        /**
         * The step of growth for the second phase will be computed as
         * average distance between two seeds / this scale
         */
        const val STEP_OF_GROWTH_SCALE = 50
        /**
         * The number of seeds generated during the active
         * seeding is computed as nSeeds * this quantity
         */
        const val N_ACTIVE_SEEDS_SCALE = 10
        /**
         * The side of seeds generated during the active seeding will
         * be computed as the step of growth used during the second phase
         * of the algorithm * this quantity
         */
        const val SIDE_ACTIVE_SEEDS_SCALE = 2
        /**
         * The step of growth used during the extension of active seeds is
         * computed as the step of growth used during the second phase
         * of the algorithm * this quantity
         */
        const val STEP_OF_GROWTH_ACTIVE_SEEDS_SCALE = 1
    }

    /**
     * Generates a navigation mesh of an environment. A rectangular shaped
     * environment is assumed, its starting point, width and height (only positive)
     * need to be specified. Obstacles are represented with java.awt.Shapes.
     * Note that only CONVEX POLYGONAL obstacles are supported, each curved
     * segment connecting two points will be considered as a straight line
     * between them.
     */
    private fun generateNavigationMesh(envStart: Point2D, envWidth: Double, envHeight: Double, envObstacles: Collection<Shape>): Collection<ConvexPolygon> =
        generateNavigationMeshHelper(envStart, envWidth, envHeight, envObstacles).first

    /**
     * See [generateNavigationMesh]. This method allow to specify the initial
     * positions of seeds and their initial side. Note that active seeding
     * and cleaning phases won't be performed.
     */
    private fun generateNavigationMesh(envStart: Point2D, envWidth: Double, envHeight: Double, envObstacles: Collection<Shape>, seedsPositions: Collection<Point2D>, side: Double): Collection<ConvexPolygon> =
        generateNavigationMeshHelper(envStart, envWidth, envHeight, envObstacles, seedsPositions, side).first

    /**
     * Generates a navigation graph of the environment. Nodes are [ConvexPolygon]s
     * and edges are [Euclidean2DCrossing]s. The only difference from a navigation mesh
     * is that an environment's graph provides information regarding the connection
     * between convex polygons.
     */
    fun generateEnvGraph(envStart: Point2D, envWidth: Double, envHeight: Double, envObstacles: Collection<Shape>, destinations: Collection<Euclidean2DPosition>): NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing> =
        generateEnvGraph(generateNavigationMeshHelper(envStart, envWidth, envHeight, envObstacles), destinations, envObstacles)

    /**
     * See [generateEnvGraph]. This method allow to specify the positions
     * where to plant seeds and their side, as well as the side of crossings.
     * The latter quantity specify the maximum distance of two neighboring
     * areas, i.e. two areas whose distance is <= crossingSide will be considered
     * connected (if no obstacle is between them). Note that active seeding and
     * cleaning phases won't be performed.
     */
    fun generateEnvGraph(envStart: Point2D, envWidth: Double, envHeight: Double, envObstacles: Collection<Shape>, seedsPositions: Collection<Point2D>, side: Double, destinations: Collection<Euclidean2DPosition>, crossingSide: Double? = null): NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing> =
        generateEnvGraph(generateNavigationMeshHelper(envStart, envWidth, envHeight, envObstacles, seedsPositions, side), destinations, envObstacles, crossingSide)

    /*
     * This is a basic algorithm for generating an environment's graph.
     * It requires no degenerate edge or collinear points in the walkable areas.
     */
    private fun generateEnvGraph(navMesh: Pair<Collection<ExtendableConvexPolygon>, Double>, destinations: Collection<Euclidean2DPosition>, envObstacles: Collection<Shape>, crossingSide: Double? = null): NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing> {
        val walkableAreas = navMesh.first
        val step = crossingSide ?: navMesh.second
        val builder = NavigationGraphBuilder<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing>(walkableAreas.size)
        walkableAreas.forEach { builder.addNode(it) }
        walkableAreas.forEachIndexed { aIndex, a ->
            /*
             * We want to find the neighbors of a (the regions whose distance from a is
             * <= step), thus we advance each of its edges and see which regions are intersected
             */
            a.vertices().indices.forEach { i ->
                val oldEdge = a.getEdge(i)
                if (a.advanceEdge(i, step)) {
                    val intersectingRegions = walkableAreas
                        .filterIndexed { rIndex, r ->
                            rIndex != aIndex && r.intersects(a.asAwtShape())
                        }
                    val intersectingObstacles = envObstacles.filter { a.intersects(it) }
                    val size = a.vertices().size
                    /*
                     * When advancing edge i also edges i-1 and i+1 are modified
                     */
                    val prevEdge = a.getEdge((i - 1 + size) % size)
                    val advancedEdge = a.getEdge(i)
                    val nextEdge = a.getEdge((i + 1) % size)
                    a.moveEdge(i, oldEdge)
                    with(intersectingRegions) {
                        /*
                         * We consider only the basic case in which only one neighbor is found
                         * and the advanced edge is completely contained in it
                         */
                        if (this.size == 1 && !builder.edgesFrom(a).map { it.to }.contains(first()) &&
                            first().containsOrLiesOnBoundary(advancedEdge.first) &&
                            first().containsOrLiesOnBoundary(advancedEdge.second)) {
                            val neighbor = first()
                            if (intersectingObstacles.isEmpty()) {
                                builder.addEdge(a, neighbor, oldEdge)
                                /*
                                 * See [Euclidean2DCrossing], we need to find the segment on
                                 * neighbor's boundary that leads to region a.
                                 */
                                val intrudingEdge = neighbor.findIntrudingEdge(prevEdge, nextEdge)
                                val p1: Euclidean2DPosition = intersection(intrudingEdge, prevEdge).intersection.get()
                                val p2: Euclidean2DPosition = intersection(intrudingEdge, nextEdge).intersection.get()
                                builder.addEdge(neighbor, a, Euclidean2DSegment(p1, p2))
                            }
                            /*
                             * We also deal with the case in which obstacles are intersected but
                             * only if the advanced edge is axis-aligned.
                             */
                            else if (advancedEdge.isAxisAligned()) {
                                val selector: (Euclidean2DPosition) -> Double =
                                    if (advancedEdge.isXAxisAligned()) { p -> p.x } else { p -> p.y }
                                /*
                                 * Once we know the advanced edge is axis-aligned, we are only interested
                                 * in the intervals occluded by obstacles along such axis.
                                 */
                                val obstacleIntervals = intersectingObstacles
                                    .map { it.vertices() }
                                    .mapNotNull {
                                        val min = it.minBy(selector)?.run(selector)
                                        val max = it.maxBy(selector)?.run(selector)
                                        if (min == null || max == null) {
                                            null
                                        } else {
                                            Pair(min, max)
                                        }
                                    }
                                /*
                                 * Passages are the intervals not occluded by obstacles along the axis
                                 * (at the beginning, the whole edge is considered as not occluded, then
                                 * each obstacle is considered to figure out which portions of the edge
                                 * are "free")
                                 */
                                val passages = mutableListOf(
                                    Pair(advancedEdge.first.run(selector), advancedEdge.second.run(selector))
                                ).map {
                                    /*
                                     * We want ordered intervals
                                     */
                                    if (it.first > it.second) {
                                        Pair(it.second, it.first)
                                    } else it
                                }.toMutableList()
                                var index = 0
                                while (index < passages.size) {
                                    var p = passages[index]
                                    for (obs in obstacleIntervals) {
                                        val subtraction = p.subtract(obs)
                                        if (subtraction.isEmpty()) {
                                            passages.removeAt(index)
                                            index--
                                            break
                                        } else {
                                            p = subtraction.first()
                                            passages[index] = p
                                            subtraction.filter { it != p }.forEach { passages.add(it) }
                                        }
                                    }
                                    index++
                                }
                                /*
                                 * Each passage will be an edge (actually, a pair of edge, one in
                                 * each direction) in the generated graph
                                 */
                                passages
                                    .filter { !fuzzyEquals(it.first, it.second) }
                                    .forEach {
                                        val passage = with(oldEdge.first) {
                                            if (advancedEdge.isXAxisAligned()) {
                                                Pair(
                                                    Euclidean2DPosition(it.first, y),
                                                    Euclidean2DPosition(it.second, y)
                                                )
                                            } else {
                                                Pair(
                                                    Euclidean2DPosition(x, it.first),
                                                    Euclidean2DPosition(x, it.second)
                                                )
                                            }
                                        }
                                        builder.addEdge(a, neighbor, passage)
                                        val intrudingEdge = neighbor.findIntrudingEdge(prevEdge, nextEdge)
                                        val p1 = if (advancedEdge.isXAxisAligned()) {
                                            intrudingEdge.findPointOnLineGivenX(passage.first.x)
                                        } else {
                                            intrudingEdge.findPointOnLineGivenY(passage.first.y)
                                        }
                                        val p2 = if (advancedEdge.isXAxisAligned()) {
                                            intrudingEdge.findPointOnLineGivenX(passage.second.x)
                                        } else {
                                            intrudingEdge.findPointOnLineGivenY(passage.second.y)
                                        }
                                        if (p1 != null && p2 != null) {
                                            builder.addEdge(neighbor, a, Pair(p1, p2))
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
        return builder.build(destinations.toList())
    }

    /*
     * This helper function generates a navigation mesh and returns the step of growth used,
     * which may be useful for various things (e.g. generating a graph from the nav mesh).
     */
    private fun generateNavigationMeshHelper(envStart: Point2D, envWidth: Double, envHeight: Double, envObstacles: Collection<Shape>): Pair<MutableList<ExtendableConvexPolygon>, Double> {
        require(envWidth > 0.0 && envHeight > 0.0) { "invalid environment" }
        val envEnd = Point2D.Double(envStart.x + envWidth, envStart.y + envHeight)
        /*
         * first seeding
         */
        var nSeeds = this.nSeeds
        var side = sqrt((envWidth * envHeight) * AREA_TO_COVER / nSeeds)
        val (stepX, stepY) = computeSteps(envWidth, envHeight, nSeeds, side)
        var stepOfGrowth = (stepX + stepY) / 2 / STEP_OF_GROWTH_SCALE
        val walkableAreas = seedAndGrow(envStart, envEnd, envObstacles, nSeeds, side, stepOfGrowth).toMutableList()
        /*
         * active seeding
         */
        nSeeds *= N_ACTIVE_SEEDS_SCALE
        side = stepOfGrowth * SIDE_ACTIVE_SEEDS_SCALE
        stepOfGrowth *= STEP_OF_GROWTH_ACTIVE_SEEDS_SCALE
        val obstacles = envObstacles.toMutableList()
        /*
         * already generated regions are obstacles for new seeds
         */
        obstacles.addAll(walkableAreas.map { it.asAwtShape() })
        walkableAreas.addAll(seedAndGrow(envStart, envEnd, obstacles, nSeeds, side, stepOfGrowth))
        return Pair(walkableAreas, stepOfGrowth)
    }

    /*
     * Similarly to the previous function, this helper fun generates a nav mesh and returns
     * the step of growth used, but allows to specify the positions of initial seeds as well
     * as their initial side.
     */
    private fun generateNavigationMeshHelper(envStart: Point2D, envWidth: Double, envHeight: Double, envObstacles: Collection<Shape>, seedsPositions: Collection<Point2D>, side: Double): Pair<MutableList<ExtendableConvexPolygon>, Double> {
        require(envWidth > 0.0 && envHeight > 0.0) { "invalid environment" }
        val envEnd = Point2D.Double(envStart.x + envWidth, envStart.y + envHeight)
        val stepOfGrowth = side / STEP_OF_GROWTH_SCALE
        val seeds = seedsPositions.map { createRectangularSeed(it.x, it.y, side, side) }.toMutableList()
        growSeeds(seeds, envObstacles, envStart, envEnd, stepOfGrowth)
        return Pair(seeds, stepOfGrowth)
    }

    private fun seedAndGrow(envStart: Point2D, envEnd: Point2D, obstacles: Collection<Shape>, nSeeds: Int, side: Double, stepOfGrowth: Double): MutableCollection<ExtendableConvexPolygon> {
        val (stepX, stepY) = computeSteps(envEnd.x - envStart.x, envEnd.y - envStart.y, nSeeds, side)
        val seeds = seedEnvironment(envStart, envEnd, side, stepX, stepY).toMutableList()
        growSeeds(seeds, obstacles, envStart, envEnd, stepOfGrowth)
        combineAdjacentRegions(seeds, obstacles, stepOfGrowth)
        return seeds
    }

    /*
     * Fills the the environment with seeds, namely squared regions that will grow
     * maintaining a convex shape. The pattern with which seeds are planted in the
     * environment is chess-like, in order to avoid overlapping seeds.
     *
     * The side parameter represents the side of a square-shaped seed. The step
     * parameters represent the distance at which seeds need to be placed on each
     * axis respectively.
     */
    private fun seedEnvironment(envStart: Point2D, envEnd: Point2D, side: Double, stepX: Double, stepY: Double): MutableCollection<ExtendableConvexPolygon> {
        val seeds = mutableListOf<ExtendableConvexPolygon>()
        var x = envStart.x
        while (x <= envEnd.x - side) {
            var y = envStart.y
            while (y <= envEnd.y - side) {
                seeds.add(createRectangularSeed(x, y, side, side))
                y += stepY + side
            }
            x += stepX + side
        }
        return seeds
    }

    private fun growSeeds(seeds: MutableCollection<ExtendableConvexPolygon>, envObstacles: Collection<Shape>, envStart: Point2D, envEnd: Point2D, step: Double) {
        seeds.removeIf { s -> envObstacles.any { s.intersects(it) } }
        val obstacles = envObstacles.toMutableList()
        obstacles.addAll(seeds.map { it.asAwtShape() })
        var growing = true
        while (growing) {
            growing = false
            seeds.forEachIndexed { i, s ->
                // each seed should not consider itself as an obstacle, thus it's removed
                obstacles.removeAt(envObstacles.size + i)
                val extended = s.extend(step, obstacles, envStart, envEnd)
                if (!growing) {
                    growing = extended
                }
                obstacles.add(envObstacles.size + i, s.asAwtShape())
            }
        }
    }

    /*
     * Combines adjacent regions if the resulting polygons are convex.
     *
     * The unit parameter represents the quantity considered to be a unit by the
     * deaccon algorithm. In other words, if the distance between two regions
     * is <= unit (and no obstacle is between them), these are considered adjacent.
     *
     * The algorithm works as follows: each edge of each region is given a chance
     * to advance of a quantity equals to unit. If during the advancement other
     * regions are encountered, and no obstacles are intersected, we try to
     * combine the extending region to the encountered ones.
     * Undoubtedly, there are better ways to do that. For the future, consider
     * using a convex hull.
     */
    private fun combineAdjacentRegions(regions: MutableList<ExtendableConvexPolygon>, obstacles: Collection<Shape>, unit: Double) {
        val incorporated = mutableListOf<ExtendableConvexPolygon>()
        var i = 0
        while (i < regions.size) {
            val r = regions[i]
            var hasGrown = false
            // if the region was incorporated into another region, we don't want to consider it
            if (!incorporated.contains(r)) {
                // don't want to combine each region with itself, thus it's removed and added back at the end
                regions.removeAt(i)
                var j = 0
                while (j < r.vertices().size) {
                    if (r.advanceEdge(j, unit)) {
                        val neighbors = regions.filter { !incorporated.contains(it) && it.intersects(r.asAwtShape()) }
                        if (obstacles.none { r.intersects(it) } && neighbors.isNotEmpty() && r.union(neighbors)) {
                            incorporated.addAll(neighbors)
                            hasGrown = true
                        } else {
                            r.advanceEdge(j, -unit)
                        }
                    }
                    j++
                }
                regions.add(i, r)
            }
            if (!hasGrown) { // if a region has grown we want to re consider it
                i++
            }
        }
        regions.removeAll(incorporated)
    }

    private fun createRectangularSeed(x: Double, y: Double, width: Double, height: Double): ExtendableConvexPolygon =
        ExtendableConvexPolygonImpl(mutableListOf(
            Euclidean2DPosition(x, y),
            Euclidean2DPosition(x + width, y),
            Euclidean2DPosition(x + width, y + height),
            Euclidean2DPosition(x, y + height)))

    /*
     * Computes two parameters for later phases of the algorithm: stepX and stepY.
     * They represent the distance at which generated seeds need to be placed on
     * each axis, respectively. A chess-like distribution of seeds is assumed.
     *
     * Note that by adopting a chess like distribution it is not guaranteed that
     * exactly nSeeds are generated, but a very close number.
     */
    private fun computeSteps(envWidth: Double, envHeight: Double, nSeeds: Int, side: Double): Pair<Double, Double> {
        val r = envWidth / envHeight
        val y = sqrt(nSeeds / r)
        val x = r * y
        val stepY = (envHeight - y * side) / y
        val stepX = (envWidth - x * side) / x
        return Pair(stepX, stepY)
    }

    /*
     * Subtracts a given interval from the current one.
     */
    private fun Pair<Double, Double>.subtract(i: Pair<Double, Double>): MutableList<Pair<Double, Double>> {
        val min = mutableListOf(first, second, i.first, i.second).min()!!
        if (min < 0) {
            /*
             * If there are negative values just translate the two intervals
             * in [0,N] and translate the results back in the original intervals.
             */
            return Pair(first + min, second + min)
                .subtract(Pair(i.first + min, i.second + min))
                .map { Pair(it.first - min, it.second - min) }
                .toMutableList()
        }
        if (isContained(i)) {
            return mutableListOf()
        }
        if (!intersects(i.first, i.second)) {
            return mutableListOf(this)
        }
        if (i.first <= first) {
            return mutableListOf(Pair(i.second, second))
        }
        val res = Pair(first, i.first)
        return if (i.second < second) {
            mutableListOf(res, Pair(i.second, second))
        } else {
            mutableListOf(res)
        }
    }

    /*
     * Checks whether the interval is contained in another given interval i.
     * Pair values must be ordered (i.e. first <= second).
     */
    private fun Pair<Double, Double>.isContained(i: Pair<Double, Double>): Boolean =
        i.first <= first && i.second >= second

    /*
     * Find the first edge of the polygon intruding with both the given segments.
     */
    private fun ConvexPolygon.findIntrudingEdge(s1: Euclidean2DSegment, s2: Euclidean2DSegment) =
        vertices().indices
            .map { getEdge(it) }
            .first {
                intersection(it, s1).type == SegmentsIntersectionTypes.POINT &&
                    intersection(it, s2).type == SegmentsIntersectionTypes.POINT
            }
}
