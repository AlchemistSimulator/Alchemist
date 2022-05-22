/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.monitor.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.util.ClassPathScanner;
import org.danilopianini.lang.CollectionWithCurrentElement;
import org.danilopianini.lang.ImmutableCollectionWithCurrentElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicBorders;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class raises a new JPanel which allows to graphically inject a new molecule
 * inside a node (or a group of nodes) or to modify the value of a certain molecule.
 *
 * @param <T> concentration type
 */
@Deprecated
@SuppressWarnings("unchecked")
@SuppressFBWarnings(
    value = { "SE_TRANSIENT_FIELD_NOT_RESTORED",  "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR" },
    justification =
        "This class is not meant to get serialized."
        + " This class is final."
)
public final class MoleculeInjectorGUI<T> extends JPanel {

    private static final long serialVersionUID = -375286112397911525L;
    private static final Logger L = LoggerFactory.getLogger(MoleculeInjectorGUI.class);
    private static final List<Incarnation<?, ?>> INCARNATIONS = new LinkedList<>();

    static {
        final var incarnations = ClassPathScanner.subTypesOf(Incarnation.class).stream()
            .map(it -> (Class<? extends Incarnation<?, ?>>) it)
            .collect(Collectors.toList());
        for (final Class<? extends Incarnation<?, ?>> clazz: incarnations) {
            try {
                INCARNATIONS.add(clazz.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                L.warn("Could not initialize incarnation {}", clazz);
            }
        }
    }

    private final transient CollectionWithCurrentElement<Incarnation<T, ?>> incarnation = makeIncarnation();
    private final Set<Node<T>> affectedNodes = new HashSet<>();
    private final List<JLabel> nodesLabels;
    private final JTextArea concentration;
    private final JTextArea molecule;
    private final JComboBox<Incarnation<?, ?>> selectedIncr;
    private final JButton apply = new JButton("Apply");

    /**
     * @param nodes The nodes which will be affected by the molecule injection.
     */
    public MoleculeInjectorGUI(final Set<Node<T>> nodes) {
        super();
        nodesLabels = new ArrayList<>();
        concentration = new JTextArea();
        molecule = new JTextArea();
        selectedIncr = new JComboBox<>();
        if (nodes != null && !nodes.isEmpty()) {
            affectedNodes.addAll(nodes);
            for (final Node<T> n : nodes) {
                nodesLabels.add(new JLabel(n.toString()));
            }
            buildView();
        }
    }

    @SuppressWarnings("unchecked")
    private CollectionWithCurrentElement<Incarnation<T, ?>> makeIncarnation() {
        return new ImmutableCollectionWithCurrentElement<>(
                INCARNATIONS.stream().map(i -> (Incarnation<T, ?>) i).collect(Collectors.toList()),
                (Incarnation<T, ?>) INCARNATIONS.get(0));
    }

    private void buildView() {
        final JPanel nodesPanel = new JPanel();
        final GridBagConstraints nodesCnst = new GridBagConstraints();
        nodesCnst.gridx = 0;
        nodesCnst.gridy = 0;
        nodesCnst.fill = GridBagConstraints.BOTH;
        nodesPanel.setLayout(new GridBagLayout());
        for (final JLabel l : nodesLabels) {
            l.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            l.setAlignmentY(JLabel.CENTER_ALIGNMENT);
            nodesPanel.add(l, nodesCnst);
            nodesCnst.gridy++;
        }
        final JScrollPane jsp = new JScrollPane(
                nodesPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().width * 2, jsp.getPreferredSize().height));
        jsp.setAutoscrolls(true);
        add(jsp);
        for (final Incarnation<?, ?> inc : MoleculeInjectorGUI.INCARNATIONS) {
            selectedIncr.addItem(inc);
        }
        add(selectedIncr);
        molecule.setBorder(BasicBorders.getTextFieldBorder());
        molecule.setText("Insert the molecule here");
        molecule.setPreferredSize(molecule.getPreferredSize());
        concentration.setBorder(BasicBorders.getTextFieldBorder());
        concentration.setText("Insert the concentration here");
        concentration.setPreferredSize(concentration.getPreferredSize());
        add(molecule);
        add(concentration);
        add(apply);
        buildActionListeners();
        final Component parent = getParent();
        if (parent instanceof JFrame) {
            ((JFrame) parent).setResizable(false);
        }
        setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void buildActionListeners() {
        concentration.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                concentration.selectAll();
            }
        });
        molecule.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                molecule.selectAll();
            }
        });
        apply.addActionListener(event -> {
            final String mol = molecule.getText();
            final String conc = concentration.getText();
            final Incarnation<T, ?> currentInc = incarnation.getCurrent();
            for (final Node<T> n : affectedNodes) {
                try {
                    n.setConcentration(currentInc.createMolecule(mol), currentInc.createConcentration(conc));
                } catch (Exception | AbstractMethodError e) { // NOPMD
                    L.error("Unable to set new concentration: ", e);
                }
            }
        });
        selectedIncr.addActionListener(
                event -> incarnation.setCurrent((Incarnation<T, ?>) selectedIncr.getSelectedItem())
        );
    }
}
