package it.unibo.alchemist.model.implementations.movestrategies.target;

import java.util.Objects;

import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy;
import it.unibo.alchemist.model.interfaces.Route;
import it.unibo.alchemist.model.interfaces.StrategyWithGPS;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * This strategy follows a {@link Route}.
 * 
 * @param <T>
 */
public class FollowTrace<T> implements TargetSelectionStrategy<T>, StrategyWithGPS {

    private static final long serialVersionUID = -446053307821810437L;
    private final Reaction<T> reaction;
    private GPSTrace trace;

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
        assert trace.getNextPosition(time) != null;
        return trace.getNextPosition(time);
    }

    @Override
    public void setTrace(final GPSTrace trace) {
        if (this.trace == null) {
            this.trace = Objects.requireNonNull(trace);
        } else {
            throw new IllegalStateException("The GPS trace can be set only once");
        }
    }

}
