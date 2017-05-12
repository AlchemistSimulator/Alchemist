package it.unibo.alchemist.boundary.gui.view;

import java.util.ArrayList;
import java.util.List;

import com.jfoenix.controls.JFXToggleButton;

import it.unibo.alchemist.boundary.gui.FXResourceLoader;
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
import jiconfont.icons.GoogleMaterialDesignIcons;

/**
 * Abstract class that models a ListView Cell to represent {@link Effect}s or
 * {@link EffectGroup}s.
 * 
 * @param <T>
 *            the generic class that will be inside the cell; it should be
 *            {@link Effect} or {@link EffectGroup}
 */
public abstract class AbstractEffectCell<T> extends ListCell<T> {
    private static final double DRAG_N_DROP_TARGET_OPACITY = 0.3;

    /**
     * Default offset of the first injected node.
     */
    protected static final int DEFAULT_OFFSET = 1;
    private final GridPane pane;
    private final int injectedNodes;

    /**
     * Default constructor. The class accepts many nodes that will be injected
     * between the {@link Label} that acts as an handle for Drag'n'Drop and the
     * visibility toggle.
     * 
     * @param nodes
     *            the nodes to inject
     */
    @SuppressWarnings("unchecked") // The item from the dragboard should be of specified class
    public AbstractEffectCell(final Node... nodes) {
        super();

        pane = new GridPane();

        final Label handle = new Label();
        handle.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.DRAG_HANDLE));
        pane.add(handle, 0, 0);

        int i = DEFAULT_OFFSET;
        for (final Node node : nodes) {
            pane.add(node, i, 0);
            i++;
        }
        this.injectedNodes = i - DEFAULT_OFFSET;

        final JFXToggleButton visibilityToggle = new JFXToggleButton();
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

                // TODO check

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

    /**
     * Returns a node in the root of the cell. It considers all the nodes.
     * 
     * @param position
     *            the position of the node considering representation in layout
     * @return the specified node
     * @throws IllegalArgumentException
     *             if a wrong position is specified
     */
    protected Node getNodeAt(final int position) {
        try {
            return this.pane.getChildren().get(position);
        } catch (final IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Wrong position specified", e);
        }
    }

    /**
     * Returns a node in the root of the cell. The position is calculated
     * ignoring nodes not injected with constructor
     * 
     * @param position
     *            the position of the node considering order in constructor
     * @return the specified node
     * @throws IllegalArgumentException
     *             if a wrong position is specified
     */
    protected Node getInjectedNodeAt(final int position) {
        if (position > 0 && position < injectedNodes) {
            return getNodeAt(DEFAULT_OFFSET + position);
        } else {
            throw new IllegalArgumentException("Wrong position specified; consider using getNodeAt() method instead");
        }
    }

    /**
     * Returns the {@link DataFormat} of the object contained in the cell.
     * 
     * @return the DataFormat
     */
    protected abstract DataFormat getDataFormat();
}
