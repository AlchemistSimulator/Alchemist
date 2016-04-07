package it.unibo.alchemist.loader.export;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to translate statistics names into a {@link UnivariateStatistic}.
 */
public final class StatUtil {

    private static final Set<Class<? extends UnivariateStatistic>> STATISTICS = new Reflections()
            .getSubTypesOf(UnivariateStatistic.class);
    private static final Logger L = LoggerFactory.getLogger(StatUtil.class);

    private StatUtil() {
    }

    /**
     * @param name
     *            the statistic
     * @return a new instance of the corresponding {@link UnivariateStatistic}
     *         wrapped in a {@link Optional}, if one exists;
     *         {@link Optional#empty()} otherwise.
     */
    public static Optional<UnivariateStatistic> makeUnivariateStatistic(final String name) {
        return STATISTICS.parallelStream()
            .filter(stat -> stat.getSimpleName().equalsIgnoreCase(name))
            .findAny()
            .flatMap(clazz -> {
                try {
                    return Optional.of(clazz.newInstance());
                } catch (IllegalAccessException | InstantiationException e) {
                    L.error("Could not initialize with empty constructor " + clazz, e);
                }
                return Optional.<UnivariateStatistic>empty();
            });
    }

}
