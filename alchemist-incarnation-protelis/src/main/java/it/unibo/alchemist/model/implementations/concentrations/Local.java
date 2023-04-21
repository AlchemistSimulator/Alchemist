/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.concentrations;

import it.unibo.alchemist.model.Concentration;

/**
 */
public final class Local implements Concentration<Object> {

    private static final long serialVersionUID = 4137133493821814841L;
    private final Object content;

    /**
     * 
     */
    public Local() {
        this(null);
    }

    /**
     * Builds a new concentration.
     * 
     * @param t
     *            the concentration value
     */
    public Local(final Object t) {
        Object temp = t;
        if (t instanceof String) {
            final String ts = (String) t;
            try {
                temp = Double.parseDouble(ts);
            } catch (NumberFormatException e) {
                if ("true".equalsIgnoreCase(ts) || "false".equalsIgnoreCase(ts)) {
                    temp = Boolean.parseBoolean(ts);
                }
            }
        }
        content = temp;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public String toString() {
        if (content == null) {
            return "null";
        }
        return content.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Local) {
            final Local l = (Local) obj;
            if (content == null) {
                return l.content == null;
            }
            return content.equals(l.content);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

}
