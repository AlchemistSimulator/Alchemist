package it.unibo.alchemist.model.implementations.movestrategies.target;

import it.unibo.alchemist.model.implementations.movestrategies.AbstractStrategyWithGPS;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;
import it.unibo.alchemist.model.interfaces.Route;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * This strategy follows a {@link Route}.
 * 
 * @param <T>
 */
public class FollowTrace<T> extends AbstractStrategyWithGPS implements TargetSelectionStrategy<T> {

    private static final long serialVersionUID = -446053307821810437L;
    private final Reaction<T> reaction;

    /**
     * @param r
     *            the reaction
     */
    public FollowTrace(final Reaction<T> r) {
        reaction = r;
    }

    @Override
    public final Position getTarget() {
        final Time time = reaction.getTau();
        assert getTrace().getNextPosition(time) != null;
        return getTrace().getNextPosition(time);
    }
}
