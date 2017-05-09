package it.unibo.alchemist.boundary.gui.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;

import it.unibo.alchemist.boundary.gui.FXResourceLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;

public abstract class AbstractEffectCell<T> extends ListCell<T> {
    public static final int DEFAULT_OFFSET = 1;
    private VBox priorityButtons;
    private JFXButton priorityUp;
    private JFXButton priorityDown;
    private JFXToggleButton visibilityToggle;
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
        for (Node node : nodes) {
            pane.add(node, i, 0);
            i++;
        }

        visibilityToggle = new JFXToggleButton();
        pane.add(visibilityToggle, i, 0);
    }

    public Node getNodeAt(final int position) {
        return this.pane.getChildren().get(position);
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
}
