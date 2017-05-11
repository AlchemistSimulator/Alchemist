package it.unibo.alchemist.boundary.gui.view;

import java.util.ArrayList;
import java.util.List;

import com.jfoenix.controls.JFXToggleButton;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;

public abstract class AbstractEffectCell<T> extends ListCell<T> {
    private static final double DRAG_N_DROP_TARGET_OPACITY = 0.3;

    /**
     * Default offset of the first injected node.
     */
    public static final int DEFAULT_OFFSET = 1;
    private final JFXToggleButton visibilityToggle;
    private final GridPane pane;

    @SuppressWarnings("unchecked") // The item from the dragboard should be of
                                   // specified class
    public AbstractEffectCell(final Node... nodes) {
        super();
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        pane = new GridPane();

        final Label handle = new Label();
        handle.setGraphic(new IconNode(GoogleMaterialDesignIcons.DRAG_HANDLE));
        pane.add(handle, 0, 0);

        int i = DEFAULT_OFFSET;
        for (final Node node : nodes) {
            pane.add(node, i, 0);
            i++;
        }

        visibilityToggle = new JFXToggleButton();
        pane.add(visibilityToggle, i, 0);

        handle.setOnDragDetected(event -> {
            if (getItem() == null) {
                return;
            }

            final Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            final ClipboardContent content = new ClipboardContent();
            content.put(getDataFormat(), getItem());
            dragboard.setDragView(this.snapshot(null, null));
            dragboard.setContent(content);

            event.consume();
        });

        setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasContent(getDataFormat())) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasContent(getDataFormat())) {
                setOpacity(DRAG_N_DROP_TARGET_OPACITY);
            }
        });

        setOnDragExited(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasContent(getDataFormat())) {
                setOpacity(1);
            }
        });

        setOnDragDropped(event -> {
            if (getItem() == null) {
                return;
            }

            final Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasContent(getDataFormat())) {
                final ObservableList<T> items = getListView().getItems();
                final Object content = db.getContent(getDataFormat());
                final int draggedIndex = items.indexOf(content);
                final int thisIndex = items.indexOf(getItem());

                // TODO

                items.set(draggedIndex, getItem());
                items.set(thisIndex, (T) db.getContent(getDataFormat()));

                final List<T> itemsCopy = new ArrayList<>(getListView().getItems());
                getListView().getItems().setAll(itemsCopy);

                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });

        setOnDragDone(DragEvent::consume);
    }

    protected Node getNodeAt(final int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Only positive position index are allowed");
        }
        return this.pane.getChildren().get(position);
    }

    protected Node getInjectedNode(final int position) {
        return getNodeAt(DEFAULT_OFFSET + position);
    }

    public JFXToggleButton getVisibilityToggle() {
        return visibilityToggle;
    }

    public Pane getPane() {
        return this.pane;
    }

    protected abstract DataFormat getDataFormat();
}
