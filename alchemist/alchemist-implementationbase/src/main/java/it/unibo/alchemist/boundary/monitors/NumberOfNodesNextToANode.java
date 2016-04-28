/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors;

import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * @param <T>
 */
@ExportInspector
public class NumberOfNodesNextToANode<T> extends EnvironmentInspector<T> {

    private static final long serialVersionUID = 6973385303909686690L;
    private static final Logger L = LoggerFactory.getLogger(NumberOfNodesNextToANode.class);

    @ExportForGUI(nameToExport = "ID of the central node")
    private String id = "0";
    @ExportForGUI(nameToExport = "Range")
    private String range = "10.0";

    @Override
    protected double[] extractValues(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {
        try {
            return new double[] {
                    env.getNodesWithinRange(env.getNodeByID(Integer.parseInt(id)),
                    Double.parseDouble(range)).size() };
        } catch (NumberFormatException e) {
            L.warn("Error parsing numbers", e);
        }
        return new double[] { Double.NaN };
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param idn
     *            id
     */
    public void setId(final String idn) {
        this.id = idn;
    }

    /**
     * @return range
     */
    public String getRange() {
        return range;
    }

    /**
     * @param rng
     *            range
     */
    public void setRange(final String rng) {
        this.range = rng;
    }

}
