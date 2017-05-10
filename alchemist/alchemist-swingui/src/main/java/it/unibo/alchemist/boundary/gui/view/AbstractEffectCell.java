package it.unibo.alchemist.boundary.gui.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;

import it.unibo.alchemist.boundary.gui.FXResourceLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;

public abstract class AbstractEffectCell<T> extends ListCell<T> {
    public static final int DEFAULT_OFFSET = 1;
    private final VBox priorityButtons;
    private final JFXButton priorityUp;
    private final JFXButton priorityDown;
    private final JFXToggleButton visibilityToggle;
    private GridPane pane;

    public AbstractEffectCell(final Node... nodes) {
        super();
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());
        pane = new GridPane();
        priorityButtons = new VBox();

        priorityUp = new JFXButton();
        priorityUp.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.KEYBOARD_ARROW_UP));
        priorityButtons.getChildren().add(priorityUp);

        priorityDown = new JFXButton();
        priorityDown.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.KEYBOARD_ARROW_DOWN));
        priorityButtons.getChildren().add(priorityDown);

        pane = new GridPane();
        pane.add(priorityButtons, 0, 0);

        int i = DEFAULT_OFFSET;
        for (final Node node : nodes) {
            pane.add(node, i, 0);
            i++;
        }

        visibilityToggle = new JFXToggleButton();
        pane.add(visibilityToggle, i, 0);

        setOnDragDetected(event -> {
            if (getItem() == null) {
                return;
            }

            // TODO

            event.consume();
        });

        setOnDragOver(event -> {
            // TODO

            event.consume();
        });

        setOnDragEntered(event -> {
            // TODO
        });

        setOnDragExited(event -> {
            // TODO
        });

        setOnDragDropped(event -> {
            if (getItem() == null) {
                return;
            }

            final Dragboard db = event.getDragboard();
            boolean success = false;

            // TODO

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

    public JFXButton getPriorityUp() {
        return priorityUp;
    }

    public JFXButton getPriorityDown() {
        return priorityDown;
    }

    public JFXToggleButton getVisibilityToggle() {
        return visibilityToggle;
    }

    public Pane getPane() {
        return this.pane;
    }

    public abstract DataFormat getDataFormat();
}
