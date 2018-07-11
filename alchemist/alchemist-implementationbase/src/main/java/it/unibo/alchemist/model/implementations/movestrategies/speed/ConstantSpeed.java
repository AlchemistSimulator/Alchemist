package it.unibo.alchemist.model.implementations.movestrategies.speed;

import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;

/**
 * This strategy makes the node move at an average constant speed, which is
 * influenced by the {@link TimeDistribution} of the {@link Reaction} hosting
 * this {@link Action}. This action tries to normalize on the {@link Reaction}
 * rate, but if the {@link TimeDistribution} has a high variance, the movements
 * on the map will inherit this tract.
 * 
 */
public class ConstantSpeed<P extends Position<P>> implements SpeedSelectionStrategy<P> {

    private static final long serialVersionUID = 1746429998480123049L;
    private final double sp;

    /**
     * @param reaction
     *            the reaction
     * @param speed
     *            the speed, in meters/second
     */
    public ConstantSpeed(final Reaction<?> reaction, final double speed) {
        assert speed > 0 : "Speed must be positive.";
        sp = speed / reaction.getRate();
    }

    @Override
    public double getCurrentSpeed(final P target) {
        return sp;
    }

}
