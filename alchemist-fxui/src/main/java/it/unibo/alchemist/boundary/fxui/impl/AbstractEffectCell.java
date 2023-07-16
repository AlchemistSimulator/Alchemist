/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl;

import com.jfoenix.controls.JFXToggleButton;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.fxui.EffectFX;
import it.unibo.alchemist.boundary.fxui.EffectGroup;
import it.unibo.alchemist.boundary.fxui.util.FXResourceLoader;
import it.unibo.alchemist.boundary.fxui.util.SVGImages;
import it.unibo.alchemist.boundary.fxui.FXOutputMonitor;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract class that models a ListView Cell to represent
 * {@link EffectFX}s or
 * {@link EffectGroup}s.
 *
 * @param <T> the generic class that will be inside the cell; it should be
 *            {@link EffectFX} or
 *            {@link EffectGroup}
 */
@SuppressFBWarnings(
        value = { "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE" },
        justification = "A ChoiceDialog is always in its own stage."
)
public abstract class AbstractEffectCell<T> extends ListCell<T> {
    /**
     * Default offset of the first injected node.
     */
    private static final int DEFAULT_OFFSET = 2;
    private static final double DRAG_N_DROP_TARGET_OPACITY = 0.3;
    private static final String WRONG_POS = "Wrong position specified";
    private final GridPane pane;
    private final DataFormat dataFormat;
    private final int injectedNodes;
    private Optional<FXOutputMonitor<?, ?>> displayMonitor = Optional.empty();

    /**
     * Default constructor. The class accepts many nodes that will be injected
     * between the {@link Label} that acts as an handle for Drag'n'Drop and the
     * visibility toggle.
     *
     * @param dataFormat the {@link DataFormat} for this cell
     * @param nodes the nodes to inject
     */
    @SuppressFBWarnings(
            value = { "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR" },
            justification = "startDragNDrop is final"
    )
    public AbstractEffectCell(final DataFormat dataFormat, final Node... nodes) {
        super();
        this.dataFormat = dataFormat;
        pane = new GridPane();
        // Initializing drag'n'drop handle
        final Label handle = new Label();
        handle.setGraphic(FXResourceLoader.getColoredIcon(GoogleMaterialDesignIcons.DRAG_HANDLE, Color.BLACK));
        pane.add(handle, 0, 0);
        // Drag'n'Drop configurations
        handle.setOnDragDetected(this::startDragNDrop);
        setOnDragOver(this::dragNDropOver);
        setOnDragEntered(this::dragNDropEntered);
        setOnDragExited(this::dragNDropExited);
        setOnDragDropped(this::dropDragNDrop);
        setOnDragDone(DragEvent::consume);
        // Not show context menu in empty cells
        this.setOnContextMenuRequested(event -> {
            if (getItem() == null) {
                getContextMenu().hide();
            }
            event.consume();
        });
        // Adding other nodes
        int i = DEFAULT_OFFSET;
        for (final Node node : nodes) {
            pane.add(node, DEFAULT_OFFSET, i);
            i++;
        }
        GridPane.setRowSpan(handle, i);
        this.injectedNodes = i - DEFAULT_OFFSET;
    }

    /**
     * Renames some {@code Property} opening a dialog.
     *
     * @param dialogTitle   the {@link TextInputDialog#titleProperty()}  dialog} title
     * @param dialogMessage the {@link TextInputDialog#headerTextProperty()}  dialog} message
     * @param dialogContent the {@link TextInputDialog#contentTextProperty()}  dialog} content
     * @param toRename      the {@code Property} to rename
     * @see TextInputDialog
     */
    protected static void rename(
            final @Nullable String dialogTitle,
            final @Nullable String dialogMessage,
            final @Nullable String dialogContent,
            final @Nonnull StringProperty toRename
    ) {
        final TextInputDialog dialog = new TextInputDialog(toRename.get());
        dialog.setTitle(dialogTitle);
        dialog.setHeaderText(dialogMessage);
        dialog.setContentText(dialogContent);
        ((Stage) dialog.getDialogPane()
                .getScene()
                .getWindow())
                .getIcons()
                .add(SVGImages.getSvgImage(SVGImages.DEFAULT_ALCHEMIST_ICON_PATH));
        dialog.showAndWait().ifPresent(s -> Platform.runLater(() -> toRename.set(s)));
    }

    /**
     * Configures the label that would probably show the element
     * name and adds an optional listener to the {@link Label#textProperty() text property}.
     *
     * @param label    the label to setup
     * @param listener the optional listener to add to the label
     */
    protected static void setupLabel(final @Nonnull Label label, final @Nullable ChangeListener<String> listener) {
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, label.getFont().getSize()));
        Optional.ofNullable(listener).ifPresent(label.textProperty()::addListener);
    }

    /**
     * Configures a toggle and adds an optional listener to the {@link JFXToggleButton#selectedProperty()}.
     *
     * @param toggle   the toggle to setup
     * @param listener the optional listener to add to the toggle
     */
    protected static void setupToggle(
            final @Nonnull JFXToggleButton toggle,
            final @Nullable ChangeListener<Boolean> listener
    ) {
        Optional.ofNullable(listener).ifPresent(toggle.selectedProperty()::addListener);
    }

    /**
     * Removes the item of this cell from the items of the {@link ListView} that contains this cell, if any.
     */
    protected final void removeItself() {
        final T item = getItem();
        if (item != null) {
            final ListView<T> listView = getListView();
            if (listView != null) {
                listView.getItems().remove(getItem());
                getDisplayMonitor().ifPresent(FXOutputMonitor::repaint); // TODO should also recalculate?
            }
        }
    }

    /**
     * Getter method for the graphical {@link it.unibo.alchemist.boundary.OutputMonitor}.
     *
     * @return the graphical {@link it.unibo.alchemist.boundary.OutputMonitor}, if any
     */
    protected final Optional<FXOutputMonitor<?, ?>> getDisplayMonitor() {
        return displayMonitor;
    }

    /**
     * Setter method for the graphical {@link it.unibo.alchemist.boundary.OutputMonitor}.
     *
     * @param displayMonitor the graphical {@link it.unibo.alchemist.boundary.OutputMonitor} to set;
     *                       if null, it will be {@link Optional#empty() unset}
     */
    protected final void setDisplayMonitor(final @Nullable FXOutputMonitor<?, ?> displayMonitor) {
        this.displayMonitor = Optional.ofNullable(displayMonitor);
    }

    /**
     * This method configures the environment to start drag'n'drop.
     * This should not be overridden unless you want to change Drag'n'Drop
     * behavior and you now what you are doing.
     *
     * @param event the MouseEvent related to long-press on a Node
     */
    protected final void startDragNDrop(final MouseEvent event) {
        if (getItem() == null) {
            throw new IllegalStateException("Empty cell: no item found");
        }
        final Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
        final ClipboardContent content = new ClipboardContent();
        content.put(getDataFormat(), getItem());
        dragboard.setDragView(this.snapshot(null, null));
        dragboard.setContent(content);
        event.consume();
    }

    private boolean eventGestureSourceIsNotThis(final DragEvent event) {
        return !this.equals(event.getGestureSource());
    }

    /**
     * This method models the behavior on drag over.
     *
     * @param event the drag over DragEvent
     */
    private void dragNDropOver(final DragEvent event) {
        if (eventGestureSourceIsNotThis(event) && event.getDragboard().hasContent(getDataFormat())) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    }

    /**
     * This method models the behavior on drag entered.
     *
     * @param event the drag entered event
     */
    private void dragNDropEntered(final DragEvent event) {
        if (eventGestureSourceIsNotThis(event) && event.getDragboard().hasContent(getDataFormat())) {
            setOpacity(DRAG_N_DROP_TARGET_OPACITY);
        }
    }

    /**
     * This method models the behavior on drag exited.
     *
     * @param event the drag exited event
     */
    private void dragNDropExited(final DragEvent event) {
        if (eventGestureSourceIsNotThis(event) && event.getDragboard().hasContent(getDataFormat())) {
            setOpacity(1);
        }
    }

    /**
     * This method ends the drag'n'drop action.
     * This should not be overridden unless you want to change Drag'n'Drop
     * behavior and you now what you are doing.
     *
     * @param event the drag'n'drop drop event
     */
    @SuppressWarnings("unchecked") // The item from the dragboard should be of specified class
    protected final void dropDragNDrop(final DragEvent event) {
        if (getItem() == null) {
            throw new IllegalStateException("Empty cell: no item found");
        }
        final Dragboard dragboard = event.getDragboard();
        if (dragboard.hasContent(getDataFormat())) {
            final ObservableList<T> items = getListView().getItems();
            final T content = (T) dragboard.getContent(getDataFormat());
            final int draggedIndex = items.indexOf(content);
            if (draggedIndex < 0) {
                throw new IllegalStateException("Can't find the dragged item in the ListView");
            }
            final int thisIndex = items.indexOf(getItem());
            if (thisIndex < 0) {
                throw new IllegalStateException("Can't find the item of this cell in the ListView");
            }
            items.set(draggedIndex, getItem());
            items.set(thisIndex, content);
            final List<T> itemsCopy = new ArrayList<>(getListView().getItems());
            getListView().getItems().setAll(itemsCopy);
            event.setDropCompleted(true);
        } else {
            throw new IllegalStateException("No content found in DragBoard");
        }
        event.consume();
    }

    /**
     * Returns a node in the root of the cell. It considers all the nodes.
     *
     * @param position the position of the node considering representation in layout
     * @return the specified node
     * @throws IllegalArgumentException if a wrong position is specified
     */
    protected Node getNodeAt(final int position) {
        if (position < 0) {
            throw new IllegalArgumentException(WRONG_POS);
        }
        try {
            final int col, row;
            if (position == 0) {
                col = 0;
                row = 0;
            } else {
                col = DEFAULT_OFFSET;
                row = position;
            }
            for (final Node node : this.pane.getChildren()) {
                if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                    return node;
                }
            }
            throw new IllegalArgumentException(WRONG_POS);
        } catch (final IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(WRONG_POS, e);
        }
    }

    /**
     * Returns a node in the root of the cell. The position is calculated
     * ignoring nodes not injected with constructor
     *
     * @param position the position of the node considering order in constructor
     * @return the specified node
     * @throws IllegalArgumentException if a wrong position is specified
     */
    protected Node getInjectedNodeAt(final int position) {
        if (position >= 0 && position < injectedNodes) {
            return getNodeAt(DEFAULT_OFFSET + position);
        } else {
            throw new IllegalArgumentException(WRONG_POS + "; consider using getNodeAt() method instead");
        }
    }

    /**
     * Getter method for the root {@link Pane} of the cell.
     *
     * @return the root pane
     */
    protected final Pane getPane() {
        return this.pane;
    }

    /**
     * Returns the {@link DataFormat} of the object contained in the cell.
     *
     * @return the DataFormat
     */
    protected final DataFormat getDataFormat() {
        return dataFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            setGraphic(pane);
        }
    }
}
