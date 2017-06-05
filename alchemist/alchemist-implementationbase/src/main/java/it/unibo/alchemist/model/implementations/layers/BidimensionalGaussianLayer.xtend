package it.unibo.alchemist.model.implementations.layers;

import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Position;
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.Data
import it.unibo.alchemist.model.implementations.utils.BidimensionalGaussian

@Data
@Accessors(PROTECTED_GETTER, PROTECTED_SETTER)
public class BidimensionalGaussianLayer implements Layer<Double> {
	
	val BidimensionalGaussian function
	val double baseline

    public new(double centerx, double centery, double norm, double sigma) {
    	this(0, centerx, centery, norm, sigma)
    }

    public new(double baseline, double centerx, double centery, double norm, double sigma) {
    	this(baseline, centerx, centery, norm, sigma, sigma)
    }

    public new(double baseline, double centerx, double centery, double norm, double sigmax, double sigmay) {
    	function = new BidimensionalGaussian(norm, centerx, centery, sigmax, sigmay)
    	this.baseline = baseline
    }

    override getValue(Position p) {
        baseline + function.value(p.getCoordinate(0), p.getCoordinate(1));
    }
}
