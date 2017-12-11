package it.unibo.alchemist.boundary.gui.view.cells;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;
import com.jfoenix.controls.JFXToggleButton;
import it.unibo.alchemist.boundary.gui.controller.EffectBarController;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.DataFormatFactory;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import java.io.IOException;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.Nullable;

import static it.unibo.alchemist.boundary.gui.utility.ResourceLoader.getStringRes;

/**
 * This ListView cell implements the {@link AbstractEffectCell} for containing
 * an {@link EffectGroup}. It has a name that identifies the EffectGroup and
 * when clicked should open a {@link ListView} to show the {@link EffectFX
 * effects} the group is composed of.
 */
public class EffectGroupCell extends AbstractEffectCell<EffectGroup> {
    private static final String DEFAULT_NAME = getStringRes("effect_group_default_name");
    private final JFXDrawersStack stack;
    private JFXDrawer effectDrawer;
    private EffectBarController effectBarController;

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
     * @param monitor the graphical {@link OutputMonitor}
     * @param stack   the stack where to open the effects lists
     */
    public EffectGroupCell(final @Nullable FXOutputMonitor<?> monitor, final JFXDrawersStack stack) {
        this(stack);
        setupDisplayMonitor(monitor);
    }

    /**
     * Constructor.
     *
     * @param groupName the name of the EffectGroup
     * @param stack     the stack where to open the effects lists
     */
    public EffectGroupCell(final String groupName, final JFXDrawersStack stack) {
        super(new Label(groupName), new JFXToggleButton());

        this.stack = stack;

        setupLabel(getLabel(), (observable, oldValue, newValue) -> this.getItem().setName(newValue));
        setupToggle(getToggle(), (observable, oldValue, newValue) -> this.getItem().setVisibility(newValue));

        initDrawer();

        this.getPane().setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Drawer size is modified every time it's opened
                if (effectDrawer.isHidden() || effectDrawer.isHiding()) {
                    effectDrawer.setDefaultDrawerSize(stack.getWidth());
                }
                this.stack.toggle(effectDrawer);
                if (effectDrawer.isShown() || effectDrawer.isShowing()) {
                    this.stack.setContent(new JFXDrawer());
                }
            }
        });

        final ContextMenu menu = new ContextMenu();
        final MenuItem rename = new MenuItem(getStringRes("menu_item_rename"));
        rename.setOnAction(event -> {
            if (getItem() != null) {
                rename(getStringRes("rename_group_dialog_title"), getStringRes("rename_group_dialog_msg"), null, getLabel().textProperty());
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
     * @param monitor   the graphical {@link OutputMonitor}
     * @param groupName the name of the EffectGroup
     * @param stack     the stack where to open the effects lists
     */
    public EffectGroupCell(final @Nullable FXOutputMonitor<?> monitor, final String groupName, final JFXDrawersStack stack) {
        this(groupName, stack);
        setupDisplayMonitor(monitor);
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
     * Initializes a new side {@link JFXDrawer drawer} that represents the
     * {@link EffectGroup} contained in this {@code Cell} and let the user edit
     * it.
     */
    private void initDrawer() {
        effectDrawer = new JFXDrawer();
        effectDrawer.setDirection(JFXDrawer.DrawerDirection.LEFT);

        if (getDisplayMonitor().isPresent()) {
            effectBarController = new EffectBarController(getDisplayMonitor().get(), this, this.stack, this.effectDrawer);
        } else {
            effectBarController = new EffectBarController(this, this.stack, this.effectDrawer);
        }

        try {
            effectDrawer.setSidePane(FXResourceLoader.getLayout(BorderPane.class, effectBarController, EffectBarController.EFFECT_BAR_LAYOUT));
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
        final EffectGroup item = this.getItem();

        if (item == null) {
            return DataFormatFactory.getDataFormat(EffectGroup.class);
        } else {
            return DataFormatFactory.getDataFormat(item);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The side drawer opened by this cell is also rebuilt.
     */
    @Override
    protected void updateItem(final EffectGroup item, final boolean empty) {
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
