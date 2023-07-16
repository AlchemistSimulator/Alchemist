/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.cli;

import com.google.common.math.DoubleMath;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.ArrayUtils;
import org.kaikikm.threadresloader.ResourceLoader;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This support class generates a CLI interface backed by a property file.
 */
public final class CLIMaker {
    private static final Properties SYNTAX;
    private static final Options OPTIONS = new Options();
    static {
        final String base = CLIMaker.class.getPackage().getName().replace('.', '/');
        final Properties syntax = new Properties();
        try {
            syntax.load(ResourceLoader.getResourceAsStream(base + "/CLI.properties"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        SYNTAX = syntax;
        SYNTAX.keySet().stream()
            .map(key -> ArrayUtils.add(key.toString().split("_"), SYNTAX.get(key).toString()))
            .collect(Collectors.groupingBy(a -> a[0]))
            .entrySet().stream()
            .map(entry -> {
                final String optionName = entry.getKey();
                final Option.Builder optBuilder = Option.builder(optionName);
                for (final String[] feature: entry.getValue()) {
                    final String optionFeature = feature[1];
                    final String optionValue = feature[2];
                    switch (optionFeature) {
                        case "longName":
                            optBuilder.longOpt(optionValue);
                            break;
                        case "description":
                            optBuilder.desc(optionValue);
                            break;
                        case "argNumber":
                            final double num = Double.parseDouble(optionValue);
                            if (Double.isInfinite(num) && num > 0) {
                                optBuilder.hasArgs();
                            } else {
                                if (!DoubleMath.isMathematicalInteger(num)) {
                                    throw new IllegalStateException(optionValue + " is not an integer.");
                                }
                                optBuilder.numberOfArgs((int) num);
                            }
                            break;
                        case "argName":
                           optBuilder.argName(optionValue); 
                           break;
                        case "separator":
                            optBuilder.valueSeparator(optionValue.charAt(0)); 
                            break;
                        default: throw new IllegalStateException("Could not understand what " + optionFeature + " is.");
                    }
                }
                return optBuilder.build();
            }).forEachOrdered(OPTIONS::addOption);
    }

    /**
     * @return an Apache {@link Options} object
     */
    public static Options getOptions() {
        return OPTIONS;
    }

    private CLIMaker() { }
}
