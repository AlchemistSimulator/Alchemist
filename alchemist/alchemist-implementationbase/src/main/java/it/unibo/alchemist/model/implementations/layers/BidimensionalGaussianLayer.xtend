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
