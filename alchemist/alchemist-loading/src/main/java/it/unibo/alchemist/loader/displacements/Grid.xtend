/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.loader.displacements;

import it.unibo.alchemist.model.interfaces.Environment
import java.util.stream.StreamSupport
import org.apache.commons.math3.random.RandomGenerator
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.Data
import it.unibo.alchemist.model.interfaces.Position

/**
 * A (possibly randomized) grid of nodes.
 */
@Data
@Accessors(PUBLIC_GETTER, PROTECTED_SETTER)
class Grid<P extends Position<? extends P>> implements Displacement<P> {

	val Environment<?, P> pm
	val RandomGenerator rand
	val double xRand
	val double yRand
	val double xShift
	val double yShift
	val double xStart
	val double yStart
	val double xEnd
	val double yEnd
	val double xStep
	val double yStep

	/**
	 * @param pm
	 *            the {@link Environment}
	 * @param rand
	 *            the {@link RandomGenerator}
	 * @param xstart
	 *            the start x position
	 * @param ystart
	 *            the start y position
	 * @param xend
	 *            the end x position
	 * @param yend
	 *            the end y position
	 * @param xstep
	 *            how distant on the x axis (on average) nodes should be
	 * @param ystep
	 *            how distant on the y axis (on average) nodes should be
	 */
	new(Environment<?, P> pm, RandomGenerator rand, double xstart, double ystart, double xend, double yend, double xstep,
		double ystep) {
		this(pm, rand, xstart, ystart, xend, yend, xstep, ystep, 0, 0)
	}

	/**
	 * @param pm
	 *            the {@link Environment}
	 * @param rand
	 *            the {@link RandomGenerator}
	 * @param xstart
	 *            the start x position
	 * @param ystart
	 *            the start y position
	 * @param xend
	 *            the end x position
	 * @param yend
	 *            the end y position
	 * @param xstep
	 *            how distant on the x axis (on average) nodes should be
	 * @param ystep
	 *            how distant on the y axis (on average) nodes should be
	 * @param xrand
	 *            how randomized should be positions along the x axis
	 * @param yrand
	 *            how randomized should be positions along the y axis
	 */
	new(Environment<?, P> pm, RandomGenerator rand, double xstart, double ystart, double xend, double yend, double xstep,
		double ystep, double xrand, double yrand) {
		this(pm, rand, xstart, ystart, xend, yend, xstep, ystep, xrand, yrand, 0)
	}

	/**
	 * @param pm
	 *            the {@link Environment}
	 * @param rand
	 *            the {@link RandomGenerator}
	 * @param xstart
	 *            the start x position
	 * @param ystart
	 *            the start y position
	 * @param xend
	 *            the end x position
	 * @param yend
	 *            the end y position
	 * @param xstep
	 *            how distant on the x axis (on average) nodes should be
	 * @param ystep
	 *            how distant on the y axis (on average) nodes should be
	 * @param xrand
	 *            how randomized should be positions along the x axis
	 * @param yrand
	 *            how randomized should be positions along the y axis
	 * @param xshift
	 *            how shifted should be positions between lines
	 */
	new(Environment<?, P> pm, RandomGenerator rand, double xstart, double ystart, double xend, double yend, double xstep,
		double ystep, double xrand, double yrand, double xshift) {
		this(pm, rand, xstart, ystart, xend, yend, xstep, ystep, xrand, yrand, xshift, 0)
	}

	/**
	 * @param pm
	 *            the {@link Environment}
	 * @param rand
	 *            the {@link RandomGenerator}
	 * @param xstart
	 *            the start x position
	 * @param ystart
	 *            the start y position
	 * @param xend
	 *            the end x position
	 * @param yend
	 *            the end y position
	 * @param xstep
	 *            how distant on the x axis (on average) nodes should be
	 * @param ystep
	 *            how distant on the y axis (on average) nodes should be
	 * @param xrand
	 *            how randomized should be positions along the x axis
	 * @param yrand
	 *            how randomized should be positions along the y axis
	 * @param xshift
	 *            how shifted should be positions between lines
	 * @param yshift
	 *            how shifted should be positions along columns
	 */
	new(Environment<?, P> pm, RandomGenerator rand, double xstart, double ystart, double xend, double yend, double xstep,
		double ystep, double xrand, double yrand, double xshift, double yshift) {
		this.pm = pm
		this.rand = rand
		this.xRand = xrand
		this.yRand = yrand
		this.xShift = xshift
		this.yShift = yshift
		xStart = xstart
		yStart = ystart
		xEnd = xend
		yEnd = yend
		xStep = xstep
		yStep = ystep
	}

	override stream() {
		val xsteps = steps(xStart, xEnd, xStep);
		val ysteps = steps(yStart, yEnd, yStep);
		val positions = (1 ..< ysteps).map [ yn |
			val y = yStart + yStep * yn;
			(1 ..< xsteps).map [ xn |
				val x = xStart + xStep * xn
				val dx = xRand * (rand.nextDouble - 0.5) + yn * xShift % xStep
				val dy = yRand * (rand.nextDouble - 0.5) + xn * yShift % yStep
				pm.makePosition(x + dx, y + dy)
			]
		].flatten
		StreamSupport.stream(positions.spliterator, false)
	}

	private static def int steps(double min, double max, double step) {
		val res = Math.ceil(Math.abs((max - min) / step)) as int
		if (step * res <= Math.abs(max - min)) {
			res + 1
		} else {
			res
		}
	}

}
