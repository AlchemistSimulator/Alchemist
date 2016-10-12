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
    private static final long serialVersionUID = 4444764520770307664L;
    private final EnvironmentSupportingDeformableCells env;

    /**
     * 
     * @param node 
     * @param env 
     */
    public TensionPresent(final Node<Double> node, final Environment<Double> env) {
        super(node);
        if (!(node instanceof CircularDeformableCell)) {
            throw new IllegalArgumentException("This Condition can only be setted in a CircularDeformableCell");
        } 
        if (env instanceof EnvironmentSupportingDeformableCells) {
            this.env = (EnvironmentSupportingDeformableCells) env;
        } else {
            throw new IllegalArgumentException("This Condition can only be supported in an EnironmentSupportingDeformableCells");
        }
    }

    @Override
    public TensionPresent cloneOnNewNode(final Node<Double> n) {
        return new TensionPresent(n, env);
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityConditioning() {
        return env.getNodesWithinRange(getNode(), env.getMaxDiameterAmongDeformableCells()).stream()
                //.parallel()
                .flatMap(n -> n instanceof CellWithCircularArea 
                        ? Stream.of((CellWithCircularArea) n) 
                                : Stream.empty())
                .mapToDouble(n -> {
                    final double maxRn;
                    final double minRn;
                    final double maxRN = ((CircularDeformableCell) getNode()).getMaxRadius();
                    final double minRN = ((CircularDeformableCell) getNode()).getRadius();
                    if (n instanceof CircularDeformableCell) {
                        maxRn = ((CircularDeformableCell) n).getMaxRadius();
                        minRn = ((CircularDeformableCell) n).getRadius();
                    } else {
                        maxRn = n.getRadius();
                        minRn = maxRn;
                    }
                    final double distance = env.getDistanceBetweenNodes(n, getNode());
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
        return env.getNodesWithinRange(getNode(), env.getMaxDiameterAmongDeformableCells()).stream()
                .parallel()
                .flatMap(n -> n instanceof CellWithCircularArea 
                        ? Stream.of((CellWithCircularArea) n) 
                                : Stream.empty())
                .filter(n -> {
                    final double maxDN = ((CircularDeformableCell) getNode()).getMaxRadius();
                    if (n instanceof CircularDeformableCell) {
                        return env.getDistanceBetweenNodes(n, getNode()) < (maxDN + ((CircularDeformableCell) n).getMaxRadius());
                    } else {
                        return env.getDistanceBetweenNodes(n, getNode()) < (maxDN + n.getRadius());
                    }
                })
                .findAny()
                .isPresent();
    }

}
