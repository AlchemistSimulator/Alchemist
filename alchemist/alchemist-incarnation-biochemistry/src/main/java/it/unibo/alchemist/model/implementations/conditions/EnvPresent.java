package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * 
 *
 */
public class EnvPresent extends AbstractCondition<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Environment<Double, ?> environment;

    /**
     * 
     * @param node 
     * @param env 
     */
    public EnvPresent(final Environment<Double, ?> env, final Node<Double> node) {
        super(node);
        environment = env;
    }

    @Override
    public EnvPresent cloneCondition(final Node<Double> n, final Reaction<Double> r) {
        return new EnvPresent(environment, n);
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityConditioning() {
        return isValid() ? 1d : 0d;
    }

    @Override
    public boolean isValid() {
        return environment.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode)
                .findAny()
                .isPresent();
    }

}
