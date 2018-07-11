/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.layers;

import it.unibo.alchemist.model.interfaces.Layer;
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.Data
import it.unibo.alchemist.model.implementations.utils.BidimensionalGaussian
import it.unibo.alchemist.model.interfaces.Position2D

@Data
@Accessors(PROTECTED_GETTER, PROTECTED_SETTER)
class BidimensionalGaussianLayer<P extends Position2D<? extends P>> implements Layer<Double, P> {
	
	val BidimensionalGaussian function
	val double baseline

    new(double centerx, double centery, double norm, double sigma) {
    	this(0, centerx, centery, norm, sigma)
    }

    new(double baseline, double centerx, double centery, double norm, double sigma) {
    	this(baseline, centerx, centery, norm, sigma, sigma)
    }

    new(double baseline, double centerx, double centery, double norm, double sigmax, double sigmay) {
    	function = new BidimensionalGaussian(norm, centerx, centery, sigmax, sigmay)
    	this.baseline = baseline
    }

    override getValue(P p) {
        baseline + function.value(p.getCoordinate(0), p.getCoordinate(1));
    }
}
