package it.unibo.alchemist.boundary.gui.view.cells;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;
import com.jfoenix.controls.JFXToggleButton;
import it.unibo.alchemist.boundary.gui.controller.EffectPropertiesController;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.utility.DataFormatFactory;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.ResourceLoader;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.Nullable;

/**
 * This ListView cell implements the {@link AbstractEffectCell} for containing
 * an {@link EffectFX}. It has a name that identifies the Effect and when
 * clicked should open another view to edit effect-specific parameters.
 */
public class EffectCell extends AbstractEffectCell<EffectFX> {
    private static final String DEFAULT_NAME = ResourceLoader.getStringRes("effect_default_name");
    private final JFXDrawersStack stack;

    /**
     * Default constructor.
     *
     * @param effectName the name of the effect
     * @param stack      the stack where to open the effect properties
     */
    public EffectCell(final String effectName, final JFXDrawersStack stack) {
        super(new Label(effectName), new JFXToggleButton());

        this.stack = stack;

        this.getLabel().setTextAlignment(TextAlignment.CENTER);
        this.getLabel().setFont(Font.font(this.getLabel().getFont().getFamily(), FontWeight.BOLD, this.getLabel().getFont().getSize()));
        this.getLabel().textProperty().addListener((observable, oldValue, newValue) -> this.getItem().setName(newValue));

        this.getToggle().selectedProperty().addListener((observable, oldValue, newValue) -> this.getItem().setVisibility(newValue));

        final JFXDrawer propertiesDrawer = new JFXDrawer();
        propertiesDrawer.setDirection(JFXDrawer.DrawerDirection.LEFT);

        propertiesDrawer.setOverLayVisible(false);
        propertiesDrawer.setResizableOnDrag(false);

        this.getPane().setOnMouseClicked(event -> {
            final EffectPropertiesController propertiesController = new EffectPropertiesController(this.getItem(), this.stack,
                    propertiesDrawer);
            try {
                propertiesDrawer.setSidePane(FXResourceLoader.getLayout(BorderPane.class, propertiesController,
                        EffectPropertiesController.EFFECT_PROPERTIES_LAYOUT));
                propertiesController.effectNameProperty().bindBidirectional(this.getLabel().textProperty());
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Could not initialize side pane for properties of effect " + this.getItem().toString() + ": ", e);
            }

            // To not interfere with label double-click action
            if (event.getClickCount() != 2) {
                // Drawer size is modified every time it's opened
                if (propertiesDrawer.isHidden() || propertiesDrawer.isHiding()) {
                    propertiesDrawer.setDefaultDrawerSize(stack.getWidth());
                }
                this.stack.toggle(propertiesDrawer);
            }
        });
    }

    /**
     * Constructor.
     *
     * @param monitor the graphical {@link OutputMonitor}
     * @param stack   the stack where to open the effect properties
     */
    public EffectCell(final @Nullable FXOutputMonitor<?> monitor, final JFXDrawersStack stack) {
        this(stack);
        setupDisplayMonitor(monitor);
    }

    /**
     * Constructor.
     *
     * @param monitor    the graphical {@link OutputMonitor}
     * @param effectName the name of the effect
     * @param stack      the stack where to open the effect properties
     */
    public EffectCell(final @Nullable FXOutputMonitor<?> monitor, final String effectName, final JFXDrawersStack stack) {
        this(effectName, stack);
        setupDisplayMonitor(monitor);
    }

    /**
     * Constructor. Creates a cell with a default name.
     *
     * @param stack the stack where to open the effect properties
     */
    public EffectCell(final JFXDrawersStack stack) {
        this(DEFAULT_NAME, stack);
    }

    /**
     * Configures the graphical {@link OutputMonitor}.
     *
     * @param monitor the graphical {@link OutputMonitor}
     */
    private void setupDisplayMonitor(final @Nullable FXOutputMonitor<?> monitor) {
        setDisplayMonitor(monitor);
        getToggle().selectedProperty().addListener((observable, oldValue, newValue) -> this.getDisplayMonitor().ifPresent(d -> {
            if (!oldValue.equals(newValue)) {
                d.repaint();
            }
        }));
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
     * Returns the toggle of the visibility.
     *
     * @return the toggle
     */
    protected JFXToggleButton getToggle() {
        return (JFXToggleButton) super.getInjectedNodeAt(1);
    }

    @Override
    public DataFormat getDataFormat() {
        final EffectFX item = this.getItem();

        if (item == null || !EffectFX.class.isAssignableFrom(item.getClass())) {
            return DataFormatFactory.getDataFormat(this.getClass());
        } else {
            return DataFormatFactory.getDataFormat(EffectFX.class);
        }
    }

    @Override
    protected void updateItem(final EffectFX item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            this.getLabel().setText(item.getName());
            this.getToggle().setSelected(item.isVisible());
        }
    }

}
