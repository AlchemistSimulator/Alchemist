package it.unibo.alchemist.model.implementations.conditions;

import java.util.stream.Stream;

import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * 
 */
public class TensionPresent extends AbstractCondition<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final EnvironmentSupportingDeformableCells env;

    /**
     * 
     * @param node 
     * @param env 
     */
    public TensionPresent(final Environment<Double> env, final Node<Double> node) {
        super(node);
        if (node instanceof CircularDeformableCell) {
            if (env instanceof EnvironmentSupportingDeformableCells) {
                this.env = (EnvironmentSupportingDeformableCells) env;
            } else {
                throw new IllegalArgumentException("This Condition can only be supported in an EnironmentSupportingDeformableCells");
            }
        } else {
            throw new IllegalArgumentException("This Condition can only be setted in a CircularDeformableCell");
        } 
    }

    @Override
    public TensionPresent cloneOnNewNode(final Node<Double> n) {
        return new TensionPresent(env, n);
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityConditioning() {
        final CircularDeformableCell thisNode = (CircularDeformableCell) getNode();
        return env.getNodesWithinRange(thisNode, env.getMaxDiameterAmongCircularDeformableCells()).stream()
                //.parallel()
                .flatMap(n -> n instanceof CellWithCircularArea 
                        ? Stream.of((CellWithCircularArea) n) 
                                : Stream.empty())
                .mapToDouble(n -> {
                    final double maxRn;
                    final double minRn;
                    final double maxRN = thisNode.getMaxRadius();
                    final double minRN = thisNode.getRadius();
                    if (n instanceof CircularDeformableCell) {
                        final CircularDeformableCell cell = (CircularDeformableCell) n;
                        maxRn = cell.getMaxRadius();
                        minRn = cell.getRadius();
                    } else {
                        maxRn = n.getRadius();
                        minRn = maxRn;
                    }
                    final double distance = env.getDistanceBetweenNodes(n, thisNode);
                    if (((maxRn + maxRN) - distance) < 0) {
                        return 0;
                    } else {
                        if (maxRn == minRn && maxRN == minRN) {
                            return 1;
                        } else {
                            return ((maxRn + maxRN) - distance) / ((maxRn + maxRN) - (minRn + minRN));
                        }
                    }
                })
                .sum();
    }

    @Override
    public boolean isValid() {
        final CircularDeformableCell thisNode = (CircularDeformableCell) getNode();
        return env.getNodesWithinRange(thisNode, env.getMaxDiameterAmongCircularDeformableCells()).stream()
                .parallel()
                .flatMap(n -> n instanceof CellWithCircularArea 
                        ? Stream.of((CellWithCircularArea) n) 
                                : Stream.empty())
                .filter(n -> {
                    final double maxDN =  thisNode.getMaxRadius();
                    if (n instanceof CircularDeformableCell) {
                        return env.getDistanceBetweenNodes(n, thisNode) < (maxDN + ((CircularDeformableCell) n).getMaxRadius());
                    } else {
                        return env.getDistanceBetweenNodes(n, thisNode) < (maxDN + n.getRadius());
                    }
                })
                .findAny()
                .isPresent();
    }

}
