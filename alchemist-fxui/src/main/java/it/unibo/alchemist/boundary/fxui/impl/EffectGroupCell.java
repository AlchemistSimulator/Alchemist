/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;
import com.jfoenix.controls.JFXToggleButton;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.fxui.effects.api.EffectGroup;
import it.unibo.alchemist.boundary.fxui.util.DataFormatFactory;
import it.unibo.alchemist.boundary.fxui.util.FXResourceLoader;
import it.unibo.alchemist.boundary.fxui.monitors.api.FXOutputMonitor;
import it.unibo.alchemist.model.Position2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;

import javax.annotation.Nullable;
import java.io.IOException;

import static it.unibo.alchemist.boundary.fxui.util.ResourceLoader.getStringRes;

/**
 * This ListView cell implements the {@link AbstractEffectCell} for containing
 * an {@link EffectGroup}. It has a name that identifies the EffectGroup and
 * when clicked should open a {@link javafx.scene.control.ListView}
 * to show the {@link EffectFX effects} the group is composed of.
 *
 * @param <P> the position type
 */
public class EffectGroupCell<P extends Position2D<? extends P>> extends AbstractEffectCell<EffectGroup<P>> {
    private static final String DEFAULT_NAME = getStringRes("effect_group_default_name");
    private final JFXDrawersStack stack;
    private JFXDrawer effectDrawer;
    private EffectBarController<P> effectBarController;

    /**
     * Default constructor.
     *
     * @param stack the stack where to open the effects lists
     */
    public EffectGroupCell(final JFXDrawersStack stack) {
        this(DEFAULT_NAME, stack);
    }

    /**
     * Constructor.
     *
     * @param monitor the graphical {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor}
     * @param stack   the stack where to open the effects lists
     */
    public EffectGroupCell(final @Nullable FXOutputMonitor<?, ?> monitor, final JFXDrawersStack stack) {
        this(stack);
        setupDisplayMonitor(monitor);
    }

    /**
     * Constructor.
     *
     * @param groupName the name of the EffectGroup
     * @param stack     the stack where to open the effects lists
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public EffectGroupCell(final String groupName, final JFXDrawersStack stack) {
        super(DataFormatFactory.getDataFormat(EffectGroup.class), new Label(groupName), new JFXToggleButton());
        this.stack = stack;
        setupLabel(getLabel(), (observable, oldValue, newValue) -> this.getItem().setName(newValue));
        setupToggle(getToggle(), (observable, oldValue, newValue) -> this.getItem().setVisibility(newValue));
        initDrawer();
        this.getPane().setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Drawer size is modified every time it's opened
                if (effectDrawer.isClosing() || effectDrawer.isClosed()) {
                    effectDrawer.setDefaultDrawerSize(stack.getWidth());
                }
                this.stack.toggle(effectDrawer);
                if (effectDrawer.isOpened() || effectDrawer.isOpening()) {
                    this.stack.setContent(new JFXDrawer());
                }
            }
        });
        final ContextMenu menu = new ContextMenu();
        final MenuItem rename = new MenuItem(getStringRes("menu_item_rename"));
        rename.setOnAction(event -> {
            if (getItem() != null) {
                rename(
                        getStringRes("rename_group_dialog_title"),
                        getStringRes("rename_group_dialog_msg"),
                        null,
                        getLabel().textProperty()
                );
            }
            event.consume();
        });
        final MenuItem delete = new MenuItem(getStringRes("menu_item_delete"));
        delete.setOnAction(event -> {
            removeItself();
            event.consume();
        });
        menu.getItems().addAll(rename, delete);
        this.setContextMenu(menu);
    }

    /**
     * Constructor.
     *
     * @param monitor   the graphical {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor}
     * @param groupName the name of the EffectGroup
     * @param stack     the stack where to open the effects lists
     */
    public EffectGroupCell(
            final @Nullable FXOutputMonitor<?, ?> monitor,
            final String groupName,
            final JFXDrawersStack stack
    ) {
        this(groupName, stack);
        setupDisplayMonitor(monitor);
    }

    /**
     * Configures the graphical {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor}.
     *
     * @param monitor the graphical {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor}
     */
    private void setupDisplayMonitor(final @Nullable FXOutputMonitor<?, ?> monitor) {
        setDisplayMonitor(monitor);
        getToggle().selectedProperty().addListener((observable, oldValue, newValue) ->
            this.getDisplayMonitor().ifPresent(d -> {
                if (!oldValue.equals(newValue)) {
                    d.repaint();
                }
            })
        );
    }

    /**
     * Initializes a new side {@link JFXDrawer drawer} that represents the
     * {@link EffectGroup} contained in this {@code Cell} and let the user edit
     * it.
     */
    private void initDrawer() {
        effectDrawer = new JFXDrawer();
        effectDrawer.setDirection(JFXDrawer.DrawerDirection.LEFT);
        if (getDisplayMonitor().isPresent()) {
            effectBarController = new EffectBarController<>(
                    getDisplayMonitor().get(),
                    this,
                    this.stack,
                    this.effectDrawer
            );
        } else {
            effectBarController = new EffectBarController<>(this, this.stack, this.effectDrawer);
        }
        try {
            effectDrawer.setSidePane(
                    FXResourceLoader.getLayout(
                            effectBarController,
                            EffectBarController.EFFECT_BAR_LAYOUT
                    )
            );
        } catch (final IOException e) {
            throw new IllegalStateException("Could not initialize side pane for effects", e);
        }
        effectBarController.groupNameProperty().bindBidirectional(this.getLabel().textProperty());
        effectDrawer.setOverLayVisible(false);
        effectDrawer.setResizableOnDrag(false);
    }

    /**
     * Returns the label with the effect name.
     *
     * @return the label
     */
    protected final Label getLabel() {
        return (Label) super.getInjectedNodeAt(0);
    }

    /**
     * Returns the toggle of the visibility.
     *
     * @return the toggle
     */
    protected final JFXToggleButton getToggle() {
        return (JFXToggleButton) super.getInjectedNodeAt(1);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The side drawer opened by this cell is also rebuilt.
     */
    @Override
    protected void updateItem(final EffectGroup<P> item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            this.getLabel().setText(item.getName());
            this.getToggle().setSelected(item.isVisible());
            initDrawer();
            item.forEach(effectBarController::addEffectToGroup);
        }
    }
}
