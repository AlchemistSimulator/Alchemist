package it.unibo.alchemist.model.implementations.strategies.target;

import static org.danilopianini.lang.RegexUtil.FLOAT_PATTERN;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.strategies.TargetSelectionStrategy;

import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * This strategy reads the value of a "target" molecule and tries to interpret it as a coordinate.
 * 
 * @param <T>
 */
public class FollowTarget<T> implements TargetSelectionStrategy<T> {

    private static final long serialVersionUID = -446053307821810437L;
    private final Environment<T> environment;
    private final Node<T> node;
    private final Molecule track;

    /**
     * @param env
     *            the environment
     * @param n
     *            the node
     * @param targetMolecule
     *            the target molecule
     */
    public FollowTarget(final Environment<T> env, final Node<T> n, final Molecule targetMolecule) {
        environment = env;
        node = n;
        track = targetMolecule;
    }

    private Position getCurrentPosition() {
        return environment.getPosition(node);
    }

    @Override
    public Position getNextTarget() {
        final Optional<T> optt = Optional.ofNullable(node.getConcentration(track));
        if (optt.isPresent()) {
            final T conc = optt.get();
            if (conc instanceof LatLongPosition) {
                return (LatLongPosition) conc;
            }
            double lat = Double.NaN;
            double lon = Double.NaN;
            if (conc instanceof Iterable) {
                final Iterator<?> iterator = ((Iterable<?>) conc).iterator();
                while (iterator.hasNext() && Double.isNaN(lon)) {
                    final Object elem = iterator.next();
                    double val;
                    if (elem instanceof Number) {
                        val = ((Number) elem).doubleValue();
                    } else if (elem == null) {
                        return getCurrentPosition();
                    } else {
                        try {
                            val = Double.parseDouble(elem.toString());
                        } catch (NumberFormatException e) {
                            return getCurrentPosition();
                        }
                    }
                    if (Double.isNaN(lat)) {
                        lat = val;
                    } else {
                        lon = val;
                    }
                }
            } else {
                final Matcher m = FLOAT_PATTERN.matcher(conc instanceof CharSequence ? (CharSequence) conc : conc.toString());
                while (Double.isNaN(lon) && m.find()) {
                    final String val = m.group();
                    /*
                     * It can not fail, unless the RegexUtil utility is broken
                     */
                    if (Double.isNaN(lat)) {
                        lat = Double.parseDouble(val);
                    } else {
                        lon = Double.parseDouble(val);
                    }
                }
            }
            if (!Double.isNaN(lon)) {
                return new LatLongPosition(lat, lon);
            }
        }
        return getCurrentPosition();
    }


}
