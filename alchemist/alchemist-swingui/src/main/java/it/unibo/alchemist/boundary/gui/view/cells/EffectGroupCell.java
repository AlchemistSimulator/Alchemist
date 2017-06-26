package it.unibo.alchemist.boundary.gui.view.cells;

import java.io.IOException;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;

import it.unibo.alchemist.boundary.gui.controller.EffectBarController;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.DataFormatFactory;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * This ListView cell implements the {@link AbstractEffectCell} for containing
 * an {@link EffectGroup}. It has a name that identifies the EffectGroup and
 * when clicked should open a {@link ListView} to show the {@link EffectFX
 * effects} the group is composed of.
 */
public class EffectGroupCell extends AbstractEffectCell<EffectGroup> {
    private static final String DEFAULT_NAME = "Unnamed effect group";
    private final JFXDrawersStack stack;
    private JFXDrawer effectDrawer;
    private EffectBarController effectBarController;

    /**
     * Default constructor.
     * 
     * @param stack
     *            the stack where to open the effects lists
     */
    public EffectGroupCell(final JFXDrawersStack stack) {
        this(DEFAULT_NAME, stack);
    }

    /**
     * Constructor.
     * 
     * @param groupName
     *            the name of the EffectGroup
     * @param stack
     *            the stack where to open the effects lists
     */
    public EffectGroupCell(final String groupName, final JFXDrawersStack stack) {
        super(new Label(groupName), new JFXToggleButton(), new JFXSlider(0, 100, 100));

        this.stack = stack;

        this.getLabel().setTextAlignment(TextAlignment.CENTER);
        this.getLabel().setFont(Font.font(this.getLabel().getFont().getFamily(), FontWeight.BOLD, this.getLabel().getFont().getSize()));
        this.getLabel().textProperty().addListener((observable, oldValue, newValue) -> this.getItem().setName(newValue));

        this.getSlider().valueProperty()
                .addListener((observable, oldValue, newValue) -> this.getItem().setTransparency(newValue.intValue()));
        this.getToggle().selectedProperty().addListener((observable, oldValue, newValue) -> this.getItem().setVisibility(newValue));

        this.getLabel().setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                final Object source = click.getSource();
                final Label label;

                if (source instanceof Label) {
                    label = (Label) source;
                } else {
                    throw new IllegalStateException("EventHandler for label rename not associated to a label");
                }

                final TextInputDialog dialog = new TextInputDialog(label.getText());
                dialog.setTitle("Rename the EffectGroup");
                dialog.setHeaderText("Please enter new name:");
                dialog.setContentText(null);

                dialog.showAndWait().ifPresent(name -> label.setText(name));
            }
        });

        initDrawer();

        this.getPane().setOnMouseClicked(event -> {
            // To not interfere with label double-click action
            if (event.getClickCount() != 2) {
                // Drawer size is modified every time it's opened
                if (effectDrawer.isHidden() || effectDrawer.isHidding()) {
                    effectDrawer.setDefaultDrawerSize(stack.getWidth());
                }
                this.stack.toggle(effectDrawer);
                if (effectDrawer.isShown() || effectDrawer.isShowing()) {
                    this.stack.setContent(new JFXDrawer());
                }
            }
        });
    }

    private void initDrawer() {
        effectDrawer = new JFXDrawer();
        effectDrawer.setDirection(JFXDrawer.DrawerDirection.LEFT);

        effectBarController = new EffectBarController(this, this.stack, effectDrawer);

        try {
            effectDrawer
                    .setSidePane(FXResourceLoader.getLayout(BorderPane.class, effectBarController, EffectBarController.EFFECT_BAR_LAYOUT));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize side pane for effects", e);
        }

        effectBarController.groupNameProperty().bind(this.getLabel().textProperty());

        effectDrawer.setOverLayVisible(false);
        effectDrawer.setResizableOnDrag(false);
    }

    /**
     * Returns the label with the effect name.
     * 
     * @return the label
     */
    protected Label getLabel() {
        return (Label) super.getInjectedNodeAt(0);
    }

    /**
     * Returns the slider of the transparency.
     * 
     * @return the slider
     */
    protected JFXSlider getSlider() {
        return (JFXSlider) super.getInjectedNodeAt(2);
    }

    /**
     * Returns the toggle of the visibility.
     * 
     * @return the toggle
     */
    protected JFXToggleButton getToggle() {
        return (JFXToggleButton) super.getInjectedNodeAt(1);
    }

    @Override
    public DataFormat getDataFormat() {
        final EffectGroup item = this.getItem();

        if (item == null) {
            return DataFormatFactory.getDataFormat(EffectGroup.class);
        } else {
            return DataFormatFactory.getDataFormat(item);
        }
    }

    @Override
    protected void updateItem(final EffectGroup item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            this.getLabel().setText(item.getName());
            this.getSlider().setValue(item.getTransparency());
            this.getToggle().setSelected(item.isVisible());
            initDrawer();
            item.forEach(e -> effectBarController.addEffectToGroup(e));
        }
    }
}
