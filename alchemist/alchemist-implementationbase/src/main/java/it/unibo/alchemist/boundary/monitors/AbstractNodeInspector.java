/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.danilopianini.view.ExportForGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T>
 */
@Deprecated
public abstract class AbstractNodeInspector<T> extends EnvironmentSampler<Node<T>, T> {

    private static final long serialVersionUID = 5078169056849107817L;
    private static final Logger L = LoggerFactory.getLogger(AbstractNodeInspector.class);

    @ExportForGUI(nameToExport = "Only consider node with ID in a specific range")
    private boolean filterids;
    @ExportForGUI(nameToExport = "Range (space or minus separated)")
    private String idrange = "";
    @ExportForGUI(nameToExport = "Filter NaN values")
    private boolean filternan = true;

    private String idrangeCache;
    private int minId = Integer.MIN_VALUE;
    private int maxId = Integer.MAX_VALUE;

    @Override
    protected Iterable<Node<T>> computeSamples(final Environment<T> env, final Reaction<T> r, final Time time,
            final long step) {
        final boolean filter = filterids;
        if (filter && !idrange.equals(idrangeCache)) {
            try {
                idrangeCache = idrange;
                final StringTokenizer tk = new StringTokenizer(idrangeCache, "- ;:.,_@^?=)(/&%$!|\\");
                if (tk.hasMoreElements()) {
                    minId = Integer.parseInt(tk.nextToken());
                    if (tk.hasMoreElements()) {
                        maxId = Integer.parseInt(tk.nextToken());
                    }
                }
            } catch (NumberFormatException e) {
                L.warn("minId or maxId are not integers", e);
            }
        }
        final int fminId = minId;
        final int fmaxId = maxId;
        return env.getNodes().stream()
                .filter(node -> fminId <= node.getId() && node.getId() <= fmaxId)
                .collect(ArrayList::new, (l, el) -> l.add(el), (l1, l2) -> l1.addAll(l2));
    }

    /**
     * @return filterids
     */
    public boolean isFilteringIDs() {
        return filterids;
    }

    /**
     * @param f
     *            filterids
     */
    public void setFilterids(final boolean f) {
        this.filterids = f;
    }

    /**
     * @return idrange
     */
    public String getIdrange() {
        return idrange;
    }

    /**
     * @param range
     *            range
     */
    public void setIdrange(final String range) {
        this.idrange = range;
    }

    /**
     * @return filter nan
     */
    public boolean isFilteringNaN() {
        return filternan;
    }

    /**
     * @param f
     *            filter nan
     */
    public void setFilternan(final boolean f) {
        this.filternan = f;
    }

}
