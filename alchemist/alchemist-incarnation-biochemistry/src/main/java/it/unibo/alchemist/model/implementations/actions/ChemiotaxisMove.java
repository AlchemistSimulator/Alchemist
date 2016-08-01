package it.unibo.alchemist.model.implementations.actions;

import java.util.Optional;

import it.unibo.alchemist.model.implementations.layers.BiomolGradientLayer;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

public class ChemiotaxisMove extends AbstractMoveNode<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = -2998372934751673524L;
    private final double step;
    private final boolean ascendent;
    private final Biomolecule biomolecule;

    public ChemiotaxisMove(Environment<Double> environment, 
            final Node<Double> node, 
            final double step, 
            final boolean ascendent, 
            final Biomolecule biomol) {
        super(environment, node);
        this.step = step;
        this.ascendent = ascendent;
        biomolecule = biomol;
    }

    public ChemiotaxisMove(Environment<Double> environment, 
            final Node<Double> node, 
            final double step, 
            final boolean ascendent) {
        super(environment, node);
        this.step = step;
        this.ascendent = ascendent;
        biomolecule = new Biomolecule("B");
    }

    @Override
    public Action<Double> cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new ChemiotaxisMove(getEnvironment(), getNode(), step, ascendent, biomolecule);
    }

    @Override
    public Position getNextPosition() {
        final double[] cor = computeVersor().getCartesianCoordinates();
        return new Continuous2DEuclidean(step * cor[0], step * cor[1]);
    }

    private Position computeVersor() {
        if (getGradientLayer().isPresent()) {
            final BiomolGradientLayer l = getGradientLayer().get();
            final double steep = l.getSteep();
            if (steep == 0) {
                return new Continuous2DEuclidean(0, 0);
            }
            final double vx = l.getParameters()[0] / steep;
            final double vy = l.getParameters()[1] / steep;
            final Position v = new Continuous2DEuclidean(vx, vy);
            if (ascendent) {
                return v;
            } else {
                return new Continuous2DEuclidean(-vx, -vy);
            }
        }
        return new Continuous2DEuclidean(0, 0);
    }

    private Optional<BiomolGradientLayer> getGradientLayer() {
        return getEnvironment().getLayers().stream()
                .parallel()
                .filter(l -> l instanceof BiomolGradientLayer)
                .map(l -> ((BiomolGradientLayer) l))
                .filter(l -> l.getBiomolecule().equals(biomolecule))
                .findFirst();
    }

}
