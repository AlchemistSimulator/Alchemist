package it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.navigationmeshes.deaccon.ExtendableConvexPolygon
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
    /*
     * Generally speaking, obstacles may be categorized into the following categories:
     *
     * - Polygons (plane figures described by a finite number of straight line segments
     * connected to form a closed polygonal chain or polygonal circuit). Polygons can
     * be:
     * -- convex: these are the easiest to treat, and the only supported ones
     * -- concave: it's fairly easy to convert a concave polygon to a mesh of convex
     * ones, so they can be treated as well
     *
     * - Curves (formally, curves aren't necessarily closed. In this context, however,
     * I am referring to closed shapes with some curved segment). The algorithm does
     * not support any type of curves at present, so curved obstacles need to be
     * converted to polygonal ones (e.g. using a bounding box, eventually an arbitrarily
     * oriented minimum bounding box).
     * In the future, however, the algorithm may be modified to support this kind of
     * obstacles. In particular, the growing phase should be modified to support curves.
     * Different scenarios involving both curved convex obstacles and concave ones
     * need to be taken into account. This possible future modification is the only reason
     * why this method does not take a Collection<ConvexPolygon> as input for obstacles.
     */
    fun generateNavigationMesh(envStart: Point2D, envWidth: Double, envHeight: Double, envObstacles: Collection<Shape>): Collection<ConvexPolygon> {
        require(envWidth > 0.0 && envHeight > 0.0) { "invalid environment" }
        val envEnd = Point2D.Double(envStart.x + envWidth, envStart.y + envHeight)
        // first seeding
        var nSeeds = this.nSeeds
        var side = sqrt((envWidth * envHeight) * AREA_TO_COVER / nSeeds)
        val (stepX, stepY) = computeSteps(envWidth, envHeight, nSeeds, side)
        var stepOfGrowth = (stepX + stepY) / 2 / STEP_OF_GROWTH_SCALE
        val walkableAreas = seedAndGrow(envStart, envEnd, envObstacles, nSeeds, side, stepOfGrowth).toMutableList()
        // active seeding
        nSeeds *= N_ACTIVE_SEEDS_SCALE
        side = stepOfGrowth * SIDE_ACTIVE_SEEDS_SCALE
        stepOfGrowth *= STEP_OF_GROWTH_ACTIVE_SEEDS_SCALE
        val obstacles = envObstacles.toMutableList()
        obstacles.addAll(walkableAreas.map { it.asAwtShape() }) // already generated regions are obstacles for new seeds
        walkableAreas.addAll(seedAndGrow(envStart, envEnd, obstacles, nSeeds, side, stepOfGrowth))
        return walkableAreas
    }

    private fun seedAndGrow(envStart: Point2D, envEnd: Point2D, obstacles: Collection<Shape>, nSeeds: Int, side: Double, stepOfGrowth: Double): Collection<ConvexPolygon> {
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

    /*
     * Grows the seeds until possible.
     */
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
                growing = s.extend(step, obstacles, envStart, envEnd)
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

    private fun createRectangularSeed(x: Double, y: Double, width: Double, height: Double) =
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
}
