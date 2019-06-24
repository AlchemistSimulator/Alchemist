/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.cli;

import com.google.common.math.DoubleMath;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.getBundle;

/**
 * This support class generates a CLI interface backed by a property file.
 */
public final class CLIMaker {
    private static final ResourceBundle SYNTAX;
    private static final Options OPTIONS = new Options();
    static {
        ResourceBundle syntax;
        try {
            syntax = getBundle(CLIMaker.class.getPackage().getName() + ".CLI");
        } catch (MissingResourceException e) {
            syntax = getBundle(CLIMaker.class.getPackage().getName() + ".CLI", Locale.US);
        }
        SYNTAX = syntax;
        SYNTAX.keySet().stream()
            .map(key -> ArrayUtils.add(key.split("_"), SYNTAX.getString(key)))
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
