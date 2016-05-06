package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * This class implements a condition which checks if a junction is present or
 * not.
 */
public class GenericJunctionPresent extends AbstractCondition<Double> {

    private static final long serialVersionUID = 7903811803845809532L;

    private final Junction junction;

    /**
     * Build the condition.
     * @param junc the junction.
     * @param node the node
     */
    public GenericJunctionPresent(final Junction junc, final Node<Double> node) {
        super(node);
        junction = junc;
    }

    @Override
    public Condition<Double> cloneOnNewNode(final Node<Double> n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Context getContext() {
        // TODO Auto-generated method stub
        return Context.NEIGHBORHOOD;
    }

    @Override
    public double getPropensityConditioning() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toString() {
        return junction.toString() + " is present";
    }

}
