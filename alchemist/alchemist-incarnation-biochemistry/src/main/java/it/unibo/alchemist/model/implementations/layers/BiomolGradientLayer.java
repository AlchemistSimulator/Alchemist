package it.unibo.alchemist.model.implementations.layers;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.BiomolLayer;
import it.unibo.alchemist.model.interfaces.Position;

public class BiomolGradientLayer implements BiomolLayer {

    private final double a;
    private final double b;
    private final double c;
    private final Biomolecule biomolecule;
    private final double steep;

    public BiomolGradientLayer(final Position direction, final double unitVariation, final double offset, final Biomolecule biomol) {
        final double dirx = direction.getCoordinate(0);
        final double diry = direction.getCoordinate(1);
        final double dirModule = FastMath.sqrt(FastMath.pow(dirx, 2) + FastMath.pow(diry, 2));
        steep = unitVariation;
        // versor coordinates
        assert dirModule != 0;
        final double vx = dirx / dirModule;
        final double vy = diry / dirModule;
        // initialize the parameters of plan representing the gradient.
        c = offset;
        a = unitVariation * vx;
        b = unitVariation * vy;
        biomolecule = biomol;
    }

    @Override
    public Double getValue(Position p) {
        final double[] cord = p.getCartesianCoordinates();
        return (cord[0] * a) + (cord[1] * b) + c;
    }

    @Override
    public Biomolecule getBiomolecule() {
        return biomolecule;
    }

    @Override
    public boolean isBiomoleculePresent(Biomolecule biomol) {
        return biomol.equals(biomolecule);
    }

    @Override
    public String toString() {
        return "Layer representing a gradient of molecule " + getBiomolecule() +
                ". The equation describing this gradient is: concentration = " + a +
                "x + " + b + "y + " + c;
    }

    public double[] getParameters() {
        return new double[]{a, b, c};
    }
    
    public double getSteep() {
        return steep;
    }
}
