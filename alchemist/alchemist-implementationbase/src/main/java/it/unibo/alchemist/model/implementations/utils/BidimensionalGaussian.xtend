package it.unibo.alchemist.model.implementations.utils

import org.apache.commons.math3.analysis.BivariateFunction
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend.lib.annotations.Accessors

import static org.apache.commons.math3.util.FastMath.exp
import java.io.Serializable

@Data
@Accessors(PROTECTED_GETTER, PROTECTED_SETTER)
class BidimensionalGaussian implements BivariateFunction, Serializable {
	
	val double amplitude
	val double x0
	val double y0
	val double sigmax
	val double sigmay	
	
	override value(double x, double y) {
		val dx = x - x0
		val dy = y - y0
		val sigmaxsq = 2 * sigmax * sigmax
		val sigmaysq = 2 * sigmay * sigmay
		amplitude * exp(-(dx * dx / sigmaxsq + dy * dy / sigmaysq))
	}
	
	def double integral() {
		2 * Math.PI * amplitude * sigmax * sigmay
	}
	
}
