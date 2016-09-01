package it.unibo.alchemist.model.implementations.actions;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 *
 */
public class ChemiotacticPolarization extends AbstractAction<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = -6147634743476443019L;
    private final Environment<Double> env;
    private final Biomolecule biomol;
    private final boolean ascend;

    /**
     * Initialize a polarization activity regulated by environmental concentration of a molecule.
     * @param environment
     * @param node
     * @param biomol
     * @param ascendGrad if that parameter is true, the polarization versor of the cell will be directed in direction of the greates concentration of biomolecule in neighborhood; if it's false, the versor will be directed in the exactly the opposite direction.
     */
    public ChemiotacticPolarization(final Environment<Double> environment, final Node<Double> node, final String biomol, final boolean ascendGrad) {
        super(node);
        if (!(node instanceof CellNode)) {
            throw  new UnsupportedOperationException("Polarization can happen only in cells.");
        }
        this.env = environment;
        this.biomol = new Biomolecule(biomol);
        this.ascend = ascendGrad;
    }

    @Override
    public ChemiotacticPolarization cloneOnNewNode(Node<Double> n, Reaction<Double> r) {
        return new ChemiotacticPolarization(env, n, biomol.toString(), ascend);
    }

    @Override
    public void execute() {
        final List<Node<Double>> l = env.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode && n.contains(biomol))
                .collect(Collectors.toList());
        Position newPolVer = weightedAverage(l);
        final double newPolVerModule = FastMath.sqrt(FastMath.pow(
                newPolVer.getCoordinate(0), 2) + FastMath.pow(newPolVer.getCoordinate(1), 2)
                );
        if (newPolVerModule == 0) {
            ((CellNode) getNode()).setPolarization(newPolVer);
        } else {
            newPolVer = new Continuous2DEuclidean(newPolVer.getCoordinate(0) / newPolVerModule, newPolVer.getCoordinate(1) / newPolVerModule);
            if (ascend) {
                ((CellNode) getNode()).setPolarization(newPolVer);
            } else {
                ((CellNode) getNode()).setPolarization(new Continuous2DEuclidean(
                        -newPolVer.getCoordinate(0), 
                        -newPolVer.getCoordinate(1))
                        );
            }
        }
    }

    private Position weightedAverage(final List<Node<Double>> list) {
        if (list.isEmpty()) {
            return new Continuous2DEuclidean(0, 0);
        }
        final double denom = list.stream().mapToDouble(n -> FastMath.pow(n.getConcentration(biomol), 2)).sum();
        double xRes = 0;
        double yRes = 0;
        for (final Node<Double> n : list) {
            xRes = xRes + FastMath.pow(n.getConcentration(biomol), 2) * env.getPosition(n).getCoordinate(0);
            yRes = yRes + FastMath.pow(n.getConcentration(biomol), 2) * env.getPosition(n).getCoordinate(1);
        }
        xRes = xRes / denom;
        yRes = yRes / denom;
        final double[] nodeCoord = env.getPosition(getNode()).getCartesianCoordinates();
        return new Continuous2DEuclidean(xRes - nodeCoord[0], yRes - nodeCoord[1]);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

}
