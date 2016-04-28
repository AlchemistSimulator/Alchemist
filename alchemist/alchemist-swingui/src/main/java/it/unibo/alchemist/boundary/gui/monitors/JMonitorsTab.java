/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.gui.monitors;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.gui.tape.JTapeFeatureStack;
import it.unibo.alchemist.boundary.gui.tape.JTapeFeatureStack.Type;
import it.unibo.alchemist.boundary.gui.tape.JTapeGroup;
import it.unibo.alchemist.boundary.gui.tape.JTapeSection;
import it.unibo.alchemist.boundary.gui.tape.JTapeTab;
import it.unibo.alchemist.boundary.interfaces.GraphicalOutputMonitor;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.l10n.R;
import it.unibo.alchemist.boundary.monitors.ExportInspector;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;

/**
 * @param <T>
 */
public class JMonitorsTab<T> extends JTapeTab implements ItemListener {
    /**
     * 
     */
    private static final long serialVersionUID = -821717582498461584L;
    private static final Reflections REFLECTIONS = new Reflections("it.unibo.alchemist");
    private static final Logger L = LoggerFactory.getLogger(JMonitorsTab.class);
    private static final String MONITORS = R.getString("monitors");
    private final JButton btnAddMonitor = new JButton(R.getString("attach_monitor"));
    private final JButton btnRemMonitor = new JButton(R.getString("detach_monitor"));
    private final JComboBox<ClassItem<? extends OutputMonitor<T>>> monitorCombo = new JComboBox<>();
    private final JTapeSection monitorsFS = new JTapeFeatureStack(Type.HORIZONTAL_STACK);
    private final List<JOutputMonitorRepresentation<T>> monitors = new LinkedList<>();
    private JOutputMonitorRepresentation<T> selected;
    private Simulation<T> simulation;

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public JMonitorsTab() {
        super(MONITORS);
        REFLECTIONS.getSubTypesOf(OutputMonitor.class).forEach((c) -> {
            if (!GraphicalOutputMonitor.class.isAssignableFrom(c)
                    && !Modifier.isAbstract(c.getModifiers())
                    && c.isAnnotationPresent(ExportInspector.class)) {
                try {
                    c.getConstructor();
                    monitorCombo.addItem(new ClassItem<>((Class<? extends OutputMonitor<T>>) c));
                } catch (NoSuchMethodException e) {
                    L.warn("{} cannot be added to the GUI: it has no default constructor.", c);
                }
            }
        });
        final JTapeGroup monitorsGroup1 = new JTapeGroup(R.getString("monitors"));
        final JTapeGroup monitorsGroup2 = new JTapeGroup(R.getString("monitors"));
        final JTapeSection monFS = new JTapeFeatureStack();

        monFS.registerFeature(monitorCombo);
        final JPanel p = new JPanel();
        p.setLayout(new GridLayout(0, 2, 0, 0));
        p.add(btnAddMonitor, BorderLayout.WEST);
        p.add(btnRemMonitor, BorderLayout.EAST);
        monFS.registerFeature(p);

        monitorsGroup1.registerSection(monFS);
        monitorsGroup2.registerSection(monitorsFS);

        registerGroup(monitorsGroup1);
        registerGroup(monitorsGroup2);

        btnAddMonitor.addActionListener(e -> 
            addOutputMonitor(((ClassItem<OutputMonitor<T>>) monitorCombo.getSelectedItem()).getPayload()));
        btnRemMonitor.addActionListener(e -> {
                removeOutputMonitor(selected);
                selected = null;
            }
        );
    }

    private void addOutputMonitor(final Class<? extends OutputMonitor<T>> monClass) {
        if (OutputMonitor.class.isAssignableFrom(monClass)) {
            final OutputMonitor<T> mon;
            final JOutputMonitorRepresentation<T> repr;
            try {
                final Constructor<? extends OutputMonitor<T>> c = monClass.getConstructor();
                mon = c.newInstance();
                if (simulation != null) {
                    simulation.addOutputMonitor(mon);
                }
                repr = new JOutputMonitorRepresentation<>(mon);
                monitors.add(repr);
                monitorsFS.add(repr);
                repr.addItemListener(this);
                revalidate();
            } catch (final InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException
                    | NoSuchMethodException e) {
                L.error("Unexpected problem creating a monitor for " + monClass, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void itemStateChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            selected = (JOutputMonitorRepresentation<T>) e.getItem();
            for (final JOutputMonitorRepresentation<?> m : monitors) {
                if (!m.equals(selected) && m.isSelected()) {
                    m.setSelected(false);
                }
            }
        }
    }

    private void removeOutputMonitor(final JOutputMonitorRepresentation<T> mon) {
        if (mon != null) {
            if (simulation != null) {
                simulation.addCommand(new Engine.StateCommand<T>().stop().build());
            }
            monitors.remove(mon);
            monitorsFS.remove(mon);
            revalidate();
        }
    }

    /**
     * @param sim the simulation
     */
    @SuppressWarnings("unchecked")
    public void setSimulation(final Simulation<?> sim) {
        if (simulation != null) {
            for (final JOutputMonitorRepresentation<T> jor : monitors) {
                final OutputMonitor<T> mon = jor.getMonitor();
                simulation.removeOutputMonitor(mon);
            }
            simulation.addCommand(new Engine.StateCommand<T>().stop().build());
            simulation.waitFor(Status.STOPPED, 0, TimeUnit.MILLISECONDS);
        }
        simulation = (Simulation<T>) sim;
        for (final JOutputMonitorRepresentation<T> jor : monitors) {
            simulation.addOutputMonitor(jor.getMonitor());
        }
    }

}
